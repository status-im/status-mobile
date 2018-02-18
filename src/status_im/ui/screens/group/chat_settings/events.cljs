(ns status-im.ui.screens.group.chat-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.chats :as chats]
            [status-im.data-store.contacts :as contacts]
            [status-im.data-store.messages :as messages]
            [status-im.i18n :as i18n]
            [status-im.protocol.core :as protocol]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.random :as random]))

;;;; FX

(re-frame/reg-fx
  ::save-chat-property
  (fn [[current-chat-id property-name value]]
    (chats/save-property current-chat-id property-name value)))

(re-frame/reg-fx
  ::add-members-to-chat
  (fn [{:keys [current-chat-id selected-participants]}]
    (chats/add-contacts current-chat-id selected-participants)))

(re-frame/reg-fx
  ::remove-members-from-chat
  (fn [[current-chat-id participants]]
    (chats/remove-contacts current-chat-id participants)))

(defn system-message [message-id content]
  {:from         "system"
   :message-id   message-id
   :content      content
   :content-type constants/text-content-type})

(defn removed-participant-message [chat-id identity contact-name]
  (let [message-text (str "You've removed " (or contact-name identity))]
    (-> (system-message (random/id) message-text)
        (assoc :chat-id chat-id)
        (messages/save))))

(re-frame/reg-fx
 ::create-removing-messages
 (fn [{:keys [current-chat-id participants contacts/contacts]}]
   (doseq [participant participants]
     (let [contact-name (get-in contacts [participant :name])]
       (removed-participant-message current-chat-id participant contact-name)))))

(re-frame/reg-fx
  ::notify-about-new-members
  (fn [{:keys [current-chat-id selected-participants
               current-public-key chats web3]}]
    (let [{:keys [name contacts]} (chats current-chat-id)
          identities    (map :identity contacts)

          {:keys [public private]
           :as   new-keypair} (protocol/new-keypair!)

          group-message {:web3    web3
                         :group   {:id       current-chat-id
                                   :name     name
                                   :contacts (conj identities current-public-key)
                                   :admin    current-public-key}
                         :message {:from       current-public-key
                                   :message-id (random/id)}}]
      (re-frame/dispatch [:update-chat! {:chat-id     current-chat-id
                                         :public-key  public
                                         :private-key private}])
      (protocol/start-watching-group! {:web3     web3
                                       :group-id current-chat-id
                                       :identity current-public-key
                                       :keypair  new-keypair
                                       :callback #(re-frame/dispatch [:incoming-message %1 %2])})
      (protocol/invite-to-group!
        (-> group-message
            (assoc-in [:group :keypair] new-keypair)
            (assoc :identities selected-participants)))
      (protocol/update-group!
        (-> group-message
            (assoc-in [:group :keypair] new-keypair)
            (assoc :identities identities)))
      (doseq [identity selected-participants]
        (protocol/add-to-group! {:web3     web3
                                 :group-id current-chat-id
                                 :identity identity
                                 :keypair  new-keypair
                                 :message  {:from       current-public-key
                                            :message-id (random/id)}})))))

(re-frame/reg-fx
  ::notify-about-removing
  (fn [{:keys [web3 current-chat-id participants chats current-public-key]}]
    (let [{:keys [private public] :as new-keypair} (protocol/new-keypair!)
          {:keys [name private-key public-key]
           :as   chat} (get chats current-chat-id)
          old-keypair {:private private-key
                       :public  public-key}
          contacts    (get chat :contacts)
          identities  (-> (map :identity contacts)
                          set
                          (clojure.set/difference participants))]
      (re-frame/dispatch [:update-chat! {:chat-id     current-chat-id
                                         :private-key private
                                         :public-key  public}])
      (doseq [participant participants]
        (let [id (random/id)]
          (doseq [keypair [old-keypair new-keypair]]
            (protocol/remove-from-group!
              {:web3     web3
               :group-id current-chat-id
               :identity participant
               :keypair  keypair
               :message  {:from       current-public-key
                          :message-id id}}))))
      (protocol/start-watching-group!
        {:web3     web3
         :group-id current-chat-id
         :identity current-public-key
         :keypair  new-keypair
         :callback #(re-frame/dispatch [:incoming-message %1 %2])})
      (protocol/update-group!
        {:web3       web3
         :group      {:id       current-chat-id
                      :name     name
                      :contacts (conj identities current-public-key)
                      :admin    current-public-key
                      :keypair  new-keypair}
         :identities identities
         :message    {:from       current-public-key
                      :message-id (random/id)}}))))

;;;; Handlers

(handlers/register-handler-fx
  :show-group-chat-profile
  (fn [{db :db} [_ chat-id]]
    {:db (assoc db :new-chat-name (get-in db [:chats chat-id :name])
                   :group/group-type :chat-group)
     :dispatch [:navigate-to :group-chat-profile]}))

(handlers/register-handler-fx
  :add-new-group-chat-participants
  (fn [{{:keys [current-chat-id selected-participants] :as db} :db} _]
    (let [new-identities (map #(hash-map :identity %) selected-participants)]
      {:db (-> db
               (update-in [:chats current-chat-id :contacts] concat new-identities)
               (assoc :selected-participants #{}))
       ::add-members-to-chat (select-keys db [:current-chat-id :selected-participants])
       ::notify-about-new-members (select-keys db [:current-chat-id :selected-participants
                                                   :current-public-key :chats :web3])})))

(defn- remove-identities [collection identities]
  (remove #(identities (:identity %)) collection))

(handlers/register-handler-fx
  :remove-group-chat-participants
  (fn [{{:keys [current-chat-id] :as db} :db} [_ participants]]
    {:db (update-in db [:chats current-chat-id :contacts] remove-identities participants)
     ::remove-members-from-chat [current-chat-id participants]
     ::notify-about-removing (merge {:participants participants}
                                    (select-keys db [:web3 :current-chat-id :chats :current-public-key]))
     ::create-removing-messages (merge {:participants participants}
                                       (select-keys db [:current-chat-id :contacts/contacts]))}))

(handlers/register-handler-fx
  :set-group-chat-name
  (fn [{{:keys [current-chat-id] :as db} :db} [_ new-chat-name]]
    {:db (assoc-in db [:chats current-chat-id :name] new-chat-name)
     ::save-chat-property [current-chat-id :name new-chat-name]}))

(handlers/register-handler-fx
  :clear-history
  (fn [{{:keys [current-chat-id] :as db} :db} _]
    {:db (assoc-in db [:chats current-chat-id :messages] {})
     :delete-messages current-chat-id}))

(handlers/register-handler-fx
  :clear-history?
  (fn [_ _]
    {:show-confirmation {:title               (i18n/label :t/clear-history-confirmation)
                         :content             (i18n/label :t/clear-group-history-confirmation)
                         :confirm-button-text (i18n/label :t/clear)
                         :on-accept           #(re-frame/dispatch [:clear-history])}}))
