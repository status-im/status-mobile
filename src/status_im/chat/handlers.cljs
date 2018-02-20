(ns status-im.chat.handlers
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.models :as models]
            [status-im.i18n :as i18n]
            [status-im.protocol.core :as protocol]
            [status-im.ui.components.styles :as components.styles]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.random :as random]
            status-im.chat.events
            [status-im.utils.datetime :as datetime]))

(handlers/register-handler
  :leave-group-chat
  ;; todo order of operations tbd
  (re-frame/after (fn [_ _] (re-frame/dispatch [:navigation-replace :home])))
  (handlers/side-effect!
   (fn [{:keys [web3 current-chat-id chats current-public-key]} _]
     (let [{:keys [public-key private-key public?]} (chats current-chat-id)]
       (protocol/stop-watching-group!
        {:web3     web3
         :group-id current-chat-id})
       (when-not public?
         (protocol/leave-group-chat!
          {:web3     web3
           :group-id current-chat-id
           :keypair  {:public  public-key
                      :private private-key}
           :message  {:from       current-public-key
                      :message-id (random/id)}})))
     (re-frame/dispatch [:remove-chat current-chat-id]))))

(handlers/register-handler-fx
  :leave-group-chat?
  (fn []
    {:show-confirmation {:title               (i18n/label :t/leave-confirmation)
                         :content             (i18n/label :t/leave-group-chat-confirmation)
                         :confirm-button-text (i18n/label :t/leave)
                         :on-accept           #(re-frame/dispatch [:leave-group-chat])}}))

(handlers/register-handler :update-group-message
  (handlers/side-effect!
   (fn [{:keys [current-public-key web3 chats]}
        [_ {:keys                                [from]
            {:keys [group-id keypair timestamp]} :payload}]]
     (let [{:keys [private public]} keypair
           {:keys [group-admin is-active] :as chat} (get chats group-id)]
       (when (and (= from group-admin
                   (or (nil? chat)
                       (models/new-update? chat timestamp))))
          (re-frame/dispatch [:update-chat! {:chat-id     group-id
                                             :public-key  public
                                             :private-key private
                                             :updated-at  timestamp}])
          (when is-active
            (protocol/start-watching-group!
             {:web3     web3
              :group-id group-id
              :identity current-public-key
              :keypair  keypair
              :callback #(re-frame/dispatch [:incoming-message %1 %2])})))))))

(re-frame/reg-fx
  ::start-watching-group
  (fn [{:keys [group-id web3 current-public-key keypair]}]
    (protocol/start-watching-group!
     {:web3     web3
      :group-id group-id
      :identity current-public-key
      :keypair  keypair
      :callback #(re-frame/dispatch [:incoming-message %1 %2])})))

(handlers/register-handler-fx
  :create-new-public-chat
  [(re-frame/inject-cofx :now)]
  (fn [{:keys [db now]} [_ topic]]
    (let [exists? (boolean (get-in db [:chats topic]))
          chat    {:chat-id               topic
                   :name                  topic
                   :color                 components.styles/default-chat-color
                   :group-chat            true
                   :public?               true
                   :is-active             true
                   :timestamp             now
                   :last-to-clock-value   0
                   :last-from-clock-value 0}]
      (merge
       (when-not exists?
         {:db (assoc-in db [:chats (:chat-id chat)] chat)
          :save-chat chat
          ::start-watching-group (merge {:group-id topic}
                                        (select-keys db [:web3 :current-public-key]))})
       {:dispatch-n [[:navigate-to-clean :home]
                     [:navigate-to-chat topic]]}))))

(re-frame/reg-fx
  ::start-listen-group
  (fn [{:keys [new-chat web3 current-public-key]}]
    (let [{:keys [chat-id public-key private-key contacts name]} new-chat
          identities (mapv :identity contacts)]
      (protocol/invite-to-group!
       {:web3       web3
        :group      {:id       chat-id
                     :name     name
                     :contacts (conj identities current-public-key)
                     :admin    current-public-key
                     :keypair  {:public  public-key
                                :private private-key}}
        :identities identities
        :message    {:from       current-public-key
                     :message-id (random/id)}})
      (protocol/start-watching-group!
       {:web3     web3
        :group-id chat-id
        :identity current-public-key
        :keypair  {:public  public-key
                   :private private-key}
        :callback #(re-frame/dispatch [:incoming-message %1 %2])}))))

(defn group-name-from-contacts [contacts selected-contacts username]
  (->> (select-keys contacts selected-contacts)
       vals
       (map :name)
       (cons username)
       (string/join ", ")))

(defn prepare-group-chat
  [{:keys [current-public-key username]
    :group/keys [selected-contacts]
    :contacts/keys [contacts]} group-name timestamp]
  (let [selected-contacts'  (mapv #(hash-map :identity %) selected-contacts)
        chat-name (if-not (string/blank? group-name)
                    group-name
                    (group-name-from-contacts contacts selected-contacts username))
        {:keys [public private]} (protocol/new-keypair!)]
    {:chat-id               (random/id)
     :public-key            public
     :private-key           private
     :name                  chat-name
     :color                 components.styles/default-chat-color
     :group-chat            true
     :group-admin           current-public-key
     :is-active             true
     :timestamp             timestamp
     :contacts              selected-contacts'
     :last-to-clock-value   0
     :last-from-clock-value 0}))

(handlers/register-handler-fx
  :create-new-group-chat-and-open
  [(re-frame/inject-cofx :now)]
  (fn [{:keys [db now]} [_ group-name]]
    (let [new-chat (prepare-group-chat (select-keys db [:group/selected-contacts :current-public-key :username
                                                        :contacts/contacts])
                                       group-name
                                       now)]
      {:db (-> db
               (assoc-in [:chats (:chat-id new-chat)] new-chat)
               (assoc :group/selected-contacts #{}))
       :save-chat new-chat
       ::start-listen-group (merge {:new-chat new-chat}
                                   (select-keys db [:web3 :current-public-key]))
       :dispatch-n [[:navigate-to-clean :home]
                    [:navigate-to-chat (:chat-id new-chat)]]})))

(handlers/register-handler-fx
  :group-chat-invite-received
  (fn [{{:keys [current-public-key] :as db} :db}
       [_ {:keys                                                    [from]
           {:keys [group-id group-name contacts keypair timestamp]} :payload}]]
    (let [{:keys [private public]} keypair]
      (let [contacts' (keep (fn [ident]
                              (when (not= ident current-public-key)
                                {:identity ident})) contacts)
            chat      (get-in db [:chats group-id])
            new-chat  {:chat-id     group-id
                       :name        group-name
                       :group-chat  true
                       :group-admin from
                       :public-key  public
                       :private-key private
                       :contacts    contacts'
                       :added-to-at timestamp
                       :timestamp   timestamp
                       :is-active   true}]
        (when (or (nil? chat)
                  (models/new-update? chat timestamp))
          {::start-watching-group (merge {:group-id group-id
                                          :keypair keypair}
                                         (select-keys db [:web3 :current-public-key]))
           :dispatch (if chat
                       [:update-chat! new-chat]
                       [:add-chat group-id new-chat])})))))

(handlers/register-handler-fx
  :show-profile
  (fn [{db :db} [_ identity]]
    {:db (assoc db :contacts/identity identity)
     :dispatch [:navigate-forget :profile]}))
