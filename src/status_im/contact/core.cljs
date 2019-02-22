(ns status-im.contact.core
  (:require [status-im.accounts.db :as accounts.db]
            [status-im.contact-code.core :as contact-code]
            [status-im.contact.db :as contact.db]
            [status-im.contact.device-info :as device-info]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.mailserver.core :as mailserver]
            [status-im.transport.message.contact :as message.contact]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.partitioned-topic :as transport.topic]
            [status-im.tribute-to-talk.db :as tribute-to-talk]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]))

(fx/defn load-contacts
  [{:keys [db all-contacts]}]
  (let [contacts-list (map #(vector (:public-key %) %) all-contacts)
        contacts (into {} contacts-list)
        tr-to-talk-enabled? (-> db tribute-to-talk/get-settings tribute-to-talk/enabled?)]
    {:db (cond-> (-> db
                     (update :contacts/contacts #(merge contacts %))
                     (assoc :contacts/blocked (contact.db/get-blocked-contacts all-contacts)))
           tr-to-talk-enabled?
           (assoc :contacts/whitelist (tribute-to-talk/get-contact-whitelist all-contacts)))}))

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
    (let [contact (-> (or (build-contact cofx public-key)
                          (get-in db [:contacts/contacts public-key]))
                      (update :system-tags
                              (fnil #(conj % :contact/added) #{})))]
      (fx/merge cofx
                {:db (assoc-in db [:contacts/new-identity] "")}
                (upsert-contact contact)
                (mailserver/upsert-mailserver-topic
                 {:chat-ids [public-key]
                  :topic    transport.topic/discovery-topic-hash
                  :fetch?   false})
                (tribute-to-talk/add-to-whitelist public-key)
                (send-contact-request contact)
                (mailserver/process-next-messages-request)))))

(fx/defn create-contact
  "Create entry in contacts"
  [{:keys [db] :as cofx} public-key]
  (when (not= (get-in db [:account/account :public-key]) public-key)
    (let [contact (build-contact cofx public-key)]
      (fx/merge cofx
                {:db (assoc-in db [:contacts/new-identity] "")}
                (upsert-contact contact)))))

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

(fx/defn open-contact-toggle-list
  [{:keys [db :as cofx]}]
  (fx/merge cofx
            {:db (assoc db
                        :group/selected-contacts #{}
                        :new-chat-name "")}
            (navigation/navigate-to-cofx :contact-toggle-list nil)))

(fx/defn set-tribute
  [{:keys [db] :as cofx} public-key tribute-to-talk]
  (let [contact (-> (or (build-contact cofx public-key)
                        (get-in db [:contacts/contacts public-key]))
                    (assoc :tribute-to-talk (or tribute-to-talk
                                                {:disabled? true})))]
    {:db (assoc-in db [:contacts/contacts public-key] contact)}))
