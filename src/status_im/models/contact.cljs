(ns status-im.models.contact
  (:require [status-im.data-store.contacts :as contacts-store]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.contact :as message.v1.contact]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.utils.handlers-macro :as handlers-macro]))

(defn can-add-to-contacts? [{:keys [pending? dapp?]}]
  (and (not dapp?)
       (or pending?
           ;; it's not in the contact list at all
           (nil? pending?))))

(defn- build-contact [whisper-id {{:keys [chats] :contacts/keys [contacts]} :db}]
  (assoc (or (get contacts whisper-id)
             (utils.contacts/whisper-id->new-contact whisper-id))
         :address (utils.contacts/public-key->address whisper-id)))

(defn- own-info [db]
  (let [{:keys [name photo-path address]} (:account/account db)
        fcm-token (get-in db [:notifications :fcm-token])]
    {:name          name
     :profile-image photo-path
     :address       address
     :fcm-token     fcm-token}))

(defn- add-new-contact [{:keys [whisper-identity] :as contact} {:keys [db]}]
  (let [new-contact (assoc contact
                           :pending? false
                           :hide-contact? false
                           :public-key whisper-identity)]
    {:db            (-> db
                        (update-in [:contacts/contacts whisper-identity]
                                   merge new-contact)
                        (assoc-in [:contacts/new-identity] ""))
     :data-store/tx [(contacts-store/save-contact-tx new-contact)]}))

(defn send-contact-request [{:keys [whisper-identity pending? dapp?] :as contact} {:keys [db] :as cofx}]
  (when-not dapp?
    (if pending?
      (transport/send (message.v1.contact/map->ContactRequestConfirmed (own-info db)) whisper-identity cofx)
      (transport/send (message.v1.contact/map->ContactRequest (own-info db)) whisper-identity cofx))))

(defn add-contact [whisper-id {:keys [db] :as cofx}]
  (let [contact (build-contact whisper-id cofx)]
    (handlers-macro/merge-fx cofx
                             (add-new-contact contact)
                             (send-contact-request contact))))

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
          (handlers-macro/merge-fx cofx
                                   {:db            (update-in db [:contacts/contacts public-key]
                                                              merge contact-props)
                                    :data-store/tx [(contacts-store/save-contact-tx
                                                     contact-props)]}))))))

(def receive-contact-request handle-contact-update)
(def receive-contact-request-confirmation handle-contact-update)
(def receive-contact-update handle-contact-update)
