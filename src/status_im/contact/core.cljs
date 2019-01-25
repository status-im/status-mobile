(ns status-im.contact.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.chat.models :as chat.models]
            [status-im.contact.db :as contact.db]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.i18n :as i18n]
            [status-im.transport.message.contact :as message.contact]
            [status-im.transport.message.protocol :as protocol]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.utils :as utils]
            [status-im.transport.partitioned-topic :as transport.topic]
            [status-im.utils.config :as config]))

(fx/defn load-contacts
  [{:keys [db all-contacts]}]
  (let [contacts-list (map #(vector (:public-key %) %) all-contacts)
        contacts (into {} contacts-list)]
    {:db (update db :contacts/contacts #(merge contacts %))}))

(defn can-add-to-contacts? [{:keys [pending? dapp?]}]
  (and (not dapp?)
       (or pending?
           ;; it's not in the contact list at all
           (nil? pending?))))

(defn build-contact [{{:keys [chats] :account/keys [account]
                       :contacts/keys [contacts]} :db} public-key]
  (cond-> (or (get contacts public-key)
              (contact.db/public-key->new-contact public-key))
    (= public-key (:public-key account)) (assoc :name (:name account))))

(defn- own-info [db]
  (let [{:keys [name photo-path address]} (:account/account db)
        fcm-token (get-in db [:notifications :fcm-token])]
    {:name          name
     :profile-image photo-path
     :address       address
     :fcm-token     fcm-token}))

(fx/defn add-new-contact [{:keys [db]} {:keys [public-key] :as contact}]
  (let [new-contact (assoc contact
                           :pending? false
                           :hide-contact? false
                           :public-key public-key)]
    {:db            (-> db
                        (update-in [:contacts/contacts public-key]
                                   merge new-contact)
                        (assoc-in [:contacts/new-identity] ""))
     :data-store/tx [(contacts-store/save-contact-tx new-contact)]}))

(fx/defn send-contact-request
  [{:keys [db] :as cofx} {:keys [public-key pending? dapp?] :as contact}]
  (when-not dapp?
    (if pending?
      (protocol/send (message.contact/map->ContactRequestConfirmed (own-info db)) public-key cofx)
      (protocol/send (message.contact/map->ContactRequest (own-info db)) public-key cofx))))

(fx/defn add-contact [{:keys [db] :as cofx} public-key]
  (when (not= (get-in db [:account/account :public-key]) public-key)
    (let [contact (build-contact cofx public-key)]
      (fx/merge cofx
                (add-new-contact contact)
                (send-contact-request contact)))))

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

(fx/defn add-tag
  "add a tag to the contact"
  [{:keys [db] :as cofx}]
  (let [tag (get-in db [:ui/contact :contact/new-tag])
        public-key (get-in db [:current-chat-id])
        tags (conj (get-in db [:contacts/contacts public-key :tags] #{}) tag)]
    {:db (assoc-in db [:contacts/contacts public-key :tags] tags)
     :data-store/tx [(contacts-store/add-contact-tag-tx public-key tag)]}))

(fx/defn remove-tag
  "remove a tag from the contact"
  [{:keys [db] :as cofx} public-key tag]
  (let [tags (disj (get-in db [:contacts/contacts public-key :tags] #{}) tag)]
    {:db (assoc-in db [:contacts/contacts public-key :tags] tags)
     :data-store/tx [(contacts-store/remove-contact-tag-tx public-key tag)]}))

(defn handle-contact-update
  [public-key
   timestamp
   {:keys [name profile-image address fcm-token] :as m}
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
            contact-props    (cond->
                              {:public-key   public-key
                               :photo-path   profile-image
                               :name         name
                               :address      (or address (:address contact))
                               :last-updated timestamp-ms
                                  ;;NOTE (yenda) in case of concurrent contact request
                               :pending?     (get contact :pending? true)}
                               fcm-token (assoc :fcm-token fcm-token))]
        ;;NOTE (yenda) only update if there is changes to the contact
        (when-not (= contact-props
                     (select-keys contact [:public-key :address :photo-path
                                           :name :fcm-token :pending?]))
          {:db            (update-in db [:contacts/contacts public-key]
                                     merge contact-props)
           :data-store/tx [(contacts-store/save-contact-tx
                            contact-props)]})))))

(def receive-contact-request handle-contact-update)
(def receive-contact-request-confirmation handle-contact-update)
(def receive-contact-update handle-contact-update)

(fx/defn add-contact-and-open-chat
  [cofx public-key]
  (fx/merge cofx
            (add-contact public-key)
            (chat.models/start-chat public-key {:navigation-reset? true})))

(fx/defn hide-contact
  [{:keys [db]} public-key]
  (when (get-in db [:contacts/contacts public-key])
    {:db (assoc-in db [:contacts/contacts public-key :hide-contact?] true)}))

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
                  (add-contacts-filter contact-identity :add-contact-and-open-chat)
                  (add-contact-and-open-chat contact-identity))))))

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
    (add-contact-and-open-chat cofx new-identity)))
