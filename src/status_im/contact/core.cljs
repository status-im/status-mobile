(ns status-im.contact.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.chat.models :as chat.models]
            [clojure.set :as clojure.set]
            [status-im.contact.db :as contact.db]
            [status-im.contact.device-info :as device-info]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.i18n :as i18n]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.message.contact :as message.contact]
            [status-im.transport.message.public-chat :as transport.public-chat]
            [status-im.transport.message.protocol :as protocol]
            [status-im.contact-code.core :as contact-code]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.utils :as utils]
            [status-im.transport.partitioned-topic :as transport.topic]
            [status-im.utils.config :as config]
            [status-im.chat.models.loading :as chat.models.loading]
            [status-im.chat.models.message :as chat.models.message]
            [status-im.mailserver.core :as mailserver]))

(fx/defn load-contacts
  [{:keys [db all-contacts]}]
  (let [contacts-list (map #(vector (:public-key %) %) all-contacts)
        contacts (into {} contacts-list)]
    {:db (-> db
             (update :contacts/contacts #(merge contacts %))
             (assoc :contacts/blocked (contact.db/get-blocked-contacts all-contacts)))}))

(defn build-contact
  [{{:keys [chats] :account/keys [account]
     :contacts/keys [contacts]} :db} public-key]
  (cond-> (contact.db/public-key->contact contacts public-key)
    (= public-key (:public-key account))
    (assoc :name (:name account))))

(defn- own-info
  [db]
  (let [{:keys [name photo-path address]} (:account/account db)
        fcm-token (get-in db [:notifications :fcm-token])]
    {:name          name
     :profile-image photo-path
     :address       address
     :device-info   (device-info/all {:db db})
     :fcm-token     fcm-token}))

(fx/defn upsert-contact
  [{:keys [db] :as cofx}
   {:keys [public-key] :as contact}]
  (fx/merge cofx
            {:db            (-> db
                                (update-in [:contacts/contacts public-key] merge contact))
             :data-store/tx [(contacts-store/save-contact-tx contact)]}
            #(when (contact.db/added? contact)
               (contact-code/listen-to-chat % public-key))))

(fx/defn send-contact-request
  [{:keys [db] :as cofx} {:keys [public-key] :as contact}]
  (if (contact.db/pending? contact)
    (protocol/send (message.contact/map->ContactRequest (own-info db)) public-key cofx)
    (protocol/send (message.contact/map->ContactRequestConfirmed (own-info db)) public-key cofx)))

(fx/defn add-contact
  "Add a contact and set pending to false"
  [{:keys [db] :as cofx} public-key]
  (when (not= (get-in db [:account/account :public-key]) public-key)
    (let [contact (-> (build-contact cofx public-key)
                      (update :system-tags
                              (fnil #(conj % :contact/added) #{})))]
      (fx/merge cofx
                {:db (assoc-in db [:contacts/new-identity] "")}
                (upsert-contact contact)
                (mailserver/upsert-mailserver-topic
                 {:chat-ids [public-key]
                  :topic    transport.topic/discovery-topic-hash
                  :fetch?   false})
                (send-contact-request contact)
                (mailserver/process-next-messages-request)))))

(fx/defn add-contacts-filter [{:keys [db]} public-key action]
  (when (not= (get-in db [:account/account :public-key]) public-key)
    (let [current-public-key (get-in db [:account/account :public-key])]
      {:db
       (cond-> db
         config/partitioned-topic-enabled?
         (assoc :filters/after-adding-discovery-filter
                {:action     action
                 :public-key public-key}))

       :shh/add-discovery-filters
       {:web3           (:web3 db)
        :private-key-id current-public-key
        :topics         [{:topic    (transport.topic/partitioned-topic-hash public-key)
                          :chat-id  public-key
                          :minPow   1
                          :callback (constantly nil)}]}})))

(fx/defn change-system-tag
  "remove a system tag from the contact"
  [{:keys [db] :as cofx} public-key tag change-fn]
  (let [contact (update (get-in db [:contacts/contacts public-key])
                        :system-tags (fnil #(change-fn % tag) #{}))]
    {:db (assoc-in db [:contacts/contacts public-key] contact)
     :data-store/tx [(contacts-store/save-contact-tx contact)]}))

(fx/defn add-system-tag
  "add a system tag to the contact"
  [{:keys [db] :as cofx} public-key tag]
  (change-system-tag cofx public-key tag conj))

(fx/defn remove-system-tag
  "remove a system tag from the contact"
  [{:keys [db] :as cofx} public-key tag]
  (change-system-tag cofx public-key tag disj))

(fx/defn block-contact-confirmation
  [cofx public-key]
  {:utils/show-confirmation
   {:title (i18n/label :t/block-contact)
    :content (i18n/label :t/block-contact-details)
    :confirm-button-text (i18n/label :t/to-block)
    :on-accept #(re-frame/dispatch [:contact.ui/block-contact-confirmed public-key])}})

(defn get-removed-unseen-count
  [current-public-key user-statuses removed-messages-ids]
  (- (count removed-messages-ids)
     (count (filter (fn [[_ statuses]]
                      (= :seen
                         (:status (get statuses
                                       current-public-key))))
                    user-statuses))))

(fx/defn clean-up-chat
  [{:keys [db get-stored-user-statuses] :as cofx} chat-id removed-chat-messages]
  (let [current-public-key (accounts.db/current-public-key cofx)
        removed-messages-ids (map :message-id removed-chat-messages)
        user-statuses (get-stored-user-statuses chat-id
                                                removed-messages-ids)
        removed-unseen-count (get-removed-unseen-count current-public-key
                                                       user-statuses
                                                       removed-messages-ids)
        db (-> db
               ;; remove messages
               (update-in [:chats chat-id :messages]
                          #(apply dissoc % removed-messages-ids))
               ;; remove message statuses
               (update-in [:chats chat-id :messages-statuses]
                          #(apply dissoc % removed-messages-ids))
               ;; remove message groups
               (update-in [:chats chat-id]
                          dissoc :message-groups))]
    (fx/merge cofx
              {:db db}
              ;; update unviewed messages count
              (chat.models/upsert-chat
               {:chat-id                      chat-id
                :unviewed-messages-count
                (- (get-in db [:chats chat-id :unviewed-messages-count])
                   removed-unseen-count)})
              ;; recompute message group
              (chat.models.loading/group-chat-messages
               chat-id
               (vals (get-in db [:chats chat-id :messages]))))))

(fx/defn clean-up-chats
  [cofx removed-messages-by-chat]
  (apply fx/merge cofx
         (map (fn [[chat-id messages]]
                (clean-up-chat chat-id messages))
              removed-messages-by-chat)))

(fx/defn remove-current-chat-id
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :current-chat-id)}
            (navigation/navigate-to-clean :home {})))

(fx/defn block-contact
  [{:keys [db get-user-messages now] :as cofx} public-key]
  (let [contact (-> (contact.db/public-key->contact
                     (:contacts/contacts db)
                     public-key)
                    (assoc :last-updated now)
                    (update :system-tags conj :contact/blocked))
        user-messages (get-user-messages public-key)
        user-messages-ids (map :message-id user-messages)
        ;; we make sure to remove the 1-1 chat which we delete entirely
        removed-messages-by-chat (-> (group-by :chat-id user-messages)
                                     (dissoc public-key))
        from-one-to-one-chat? (not (get-in db [:chats (:current-chat-id db) :group-chat]))]
    (fx/merge cofx
              {:db (-> db
                       ;; add the contact to blocked contacts
                       (update :contacts/blocked conj public-key)
                       ;; update the contact in contacts list
                       (assoc-in [:contacts/contacts public-key] contact)
                       ;; remove the 1-1 chat if it exists
                       (update-in [:chats] dissoc public-key))
               :data-store/tx [(contacts-store/block-user-tx contact
                                                             user-messages-ids)]}
              ;;remove the messages from chat
              (clean-up-chats removed-messages-by-chat)
              (chat.models.message/update-last-messages
               (keys removed-messages-by-chat))
              ;; reset navigation to avoid going back to non existing one to one chat
              (if from-one-to-one-chat?
                remove-current-chat-id
                (navigation/navigate-back)))))

(fx/defn unblock-contact
  [{:keys [db now]} public-key]
  (let [contact (-> (get-in db [:contacts/contacts public-key])
                    (assoc :last-updated now)
                    (update :system-tags disj :contact/blocked))]
    {:db (-> db
             (update :contacts/blocked disj public-key)
             (assoc-in [:contacts/contacts public-key] contact))
     :data-store/tx [(contacts-store/save-contact-tx contact)]}))

(defn handle-contact-update
  [public-key
   timestamp
   {:keys [name profile-image address fcm-token device-info] :as m}
   {{:contacts/keys [contacts] :as db} :db :as cofx}]
  ;; We need to convert to timestamp ms as before we were using now in ms to
  ;; set last updated
  ;; Using whisper timestamp mostly works but breaks in a few scenarios:
  ;; 2 updates sent in the same second
  ;; when using multi-device & clocks are out of sync
  ;; Using logical clocks is probably the correct way to handle it, but an overkill
  ;; for now
  (let [timestamp-ms       (* timestamp 1000)
        prev-last-updated  (get-in db [:contacts/contacts public-key :last-updated])
        current-public-key (accounts.db/current-public-key cofx)]
    (when (and (not= current-public-key public-key)
               (< prev-last-updated timestamp-ms))
      (let [contact          (get contacts public-key)

            ;; Backward compatibility with <= 0.9.21, as they don't send
            ;; fcm-token & address in contact updates
            contact-props
            (cond-> {:public-key   public-key
                     :photo-path   profile-image
                     :name         name
                     :address      (or address
                                       (:address contact)
                                       (contact.db/public-key->address public-key))
                     :device-info  (device-info/merge-info
                                    timestamp
                                    (:device-info contact)
                                    device-info)
                     :last-updated timestamp-ms
                     :system-tags  (conj (get contact :system-tags #{})
                                         :contact/request-received)}
              fcm-token (assoc :fcm-token fcm-token))]
        (upsert-contact cofx contact-props)))))

(def receive-contact-request handle-contact-update)
(def receive-contact-request-confirmation handle-contact-update)
(def receive-contact-update handle-contact-update)

(fx/defn open-chat
  [cofx public-key]
  (fx/merge cofx
            (chat.models/start-chat public-key {:navigation-reset? true})))

(fx/defn handle-qr-code
  [{:keys [db] :as cofx} contact-identity]
  (let [current-account (:account/account db)
        fx              {:db (assoc db :contacts/new-identity contact-identity)}
        validation-result (new-chat.db/validate-pub-key db contact-identity)]
    (if (some? validation-result)
      {:utils/show-popup {:title (i18n/label :t/unable-to-read-this-code)
                          :content validation-result
                          :on-dismiss #(re-frame/dispatch [:navigate-to-clean :home])}}
      (fx/merge cofx
                fx
                (if config/partitioned-topic-enabled?
                  (add-contacts-filter contact-identity :open-chat)
                  (open-chat contact-identity))))))

(fx/defn open-contact-toggle-list
  [{:keys [db :as cofx]}]
  (fx/merge cofx
            {:db (assoc db
                        :group/selected-contacts #{}
                        :new-chat-name "")}
            (navigation/navigate-to-cofx :contact-toggle-list nil)))

(fx/defn add-new-identity-to-contacts
  [{{:contacts/keys [new-identity]} :db :as cofx}]
  (when (seq new-identity)
    (open-chat cofx new-identity)))
