(ns status-im.contact.core
  (:require [status-im.data-store.contacts :as contacts-store]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.message.contact :as message.contact]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]
            [status-im.chat.models :as chat.models]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.utils :as utils]
            [status-im.utils.fx :as fx]))

(re-frame/reg-cofx
 :get-default-contacts
 (fn [coeffects _]
   (assoc coeffects :default-contacts js-res/default-contacts)))

(fx/defn load-contacts
  [{:keys [db all-contacts]}]
  (let [contacts-list (map #(vector (:whisper-identity %) %) all-contacts)
        contacts (into {} contacts-list)]
    {:db (update db :contacts/contacts #(merge contacts %))}))

(defn can-add-to-contacts? [{:keys [pending? dapp?]}]
  (and (not dapp?)
       (or pending?
           ;; it's not in the contact list at all
           (nil? pending?))))

(defn build-contact [{{:keys [chats current-public-key]
                       :account/keys [account]
                       :contacts/keys [contacts]} :db} whisper-id]
  (cond-> (assoc (or (get contacts whisper-id)
                     (utils.contacts/whisper-id->new-contact whisper-id))
                 :address (utils.contacts/public-key->address whisper-id))

    (= whisper-id current-public-key) (assoc :name (:name account))))

(defn- own-info [db]
  (let [{:keys [name photo-path address]} (:account/account db)
        fcm-token (get-in db [:notifications :fcm-token])]
    {:name          name
     :profile-image photo-path
     :address       address
     :fcm-token     fcm-token}))

(fx/defn add-new-contact [{:keys [db]} {:keys [whisper-identity] :as contact}]
  (let [new-contact (assoc contact
                           :pending? false
                           :hide-contact? false
                           :public-key whisper-identity)]
    {:db            (-> db
                        (update-in [:contacts/contacts whisper-identity]
                                   merge new-contact)
                        (assoc-in [:contacts/new-identity] ""))
     :data-store/tx [(contacts-store/save-contact-tx new-contact)]}))

(fx/defn send-contact-request
  [{:keys [db] :as cofx} {:keys [whisper-identity pending? dapp?] :as contact}]
  (when-not dapp?
    (if pending?
      (protocol/send (message.contact/map->ContactRequestConfirmed (own-info db)) whisper-identity cofx)
      (protocol/send (message.contact/map->ContactRequest (own-info db)) whisper-identity cofx))))

(fx/defn add-contact [{:keys [db] :as cofx} whisper-id]
  (when (not= (get-in db [:account/account :public-key]) whisper-id)
    (let [contact (build-contact cofx whisper-id)]
      (fx/merge cofx
                (add-new-contact contact)
                (send-contact-request contact)))))

(fx/defn add-tag
  "add a tag to the contact"
  [{:keys [db] :as cofx}]
  (let [tag (get-in db [:ui/contact :contact/new-tag])
        whisper-id (get-in db [:current-chat-id])
        tags (conj (get-in db [:contacts/contacts whisper-id :tags] #{}) tag)]
    {:db (assoc-in db [:contacts/contacts whisper-id :tags] tags)
     :data-store/tx [(contacts-store/add-contact-tag-tx whisper-id tag)]}))

(fx/defn remove-tag
  "remove a tag from the contact"
  [{:keys [db] :as cofx} whisper-id tag]
  (let [tags (disj (get-in db [:contacts/contacts whisper-id :tags] #{}) tag)]
    {:db (assoc-in db [:contacts/contacts whisper-id :tags] tags)
     :data-store/tx [(contacts-store/remove-contact-tag-tx whisper-id tag)]}))

(defn handle-contact-update
  [public-key
   timestamp
   {:keys [name profile-image address fcm-token] :as m}
   {{:contacts/keys [contacts] :keys [current-public-key] :as db} :db :as cofx}]
  ;; We need to convert to timestamp ms as before we were using now in ms to
  ;; set last updated
  ;; Using whisper timestamp mostly works but breaks in a few scenarios:
  ;; 2 updates sent in the same second
  ;; when using multi-device & clocks are out of sync
  ;; Using logical clocks is probably the correct way to handle it, but an overkill
  ;; for now
  (let [timestamp-ms      (* timestamp 1000)
        prev-last-updated (get-in db [:contacts/contacts public-key :last-updated])]
    (when (and (not= current-public-key public-key)
               (< prev-last-updated timestamp-ms))
      (let [contact          (get contacts public-key)

            ;; Backward compatibility with <= 0.9.21, as they don't send
            ;; fcm-token & address in contact updates
            contact-props    (cond->
                              {:whisper-identity public-key
                               :public-key       public-key
                               :photo-path       profile-image
                               :name             name
                               :address          (or address
                                                     (:address contact)
                                                     (utils.contacts/public-key->address public-key))
                               :last-updated     timestamp-ms
                                  ;;NOTE (yenda) in case of concurrent contact request
                               :pending?         (get contact :pending? true)}
                               fcm-token (assoc :fcm-token fcm-token))]
        ;;NOTE (yenda) only update if there is changes to the contact
        (when-not (= contact-props
                     (select-keys contact [:whisper-identity :public-key :address
                                           :photo-path :name :fcm-token :pending?]))
          {:db            (update-in db [:contacts/contacts public-key]
                                     merge contact-props)
           :data-store/tx [(contacts-store/save-contact-tx
                            contact-props)]})))))

(def receive-contact-request handle-contact-update)
(def receive-contact-request-confirmation handle-contact-update)
(def receive-contact-update handle-contact-update)

(fx/defn add-contact-and-open-chat
  [cofx whisper-id]
  (fx/merge cofx
            (add-contact whisper-id)
            (chat.models/start-chat whisper-id {:navigation-reset? true})))

(fx/defn hide-contact
  [{:keys [db]} whisper-id]
  (when (get-in db [:contacts/contacts whisper-id])
    {:db (assoc-in db [:contacts/contacts whisper-id :hide-contact?] true)}))

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
                (add-contact-and-open-chat contact-identity)))))

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
