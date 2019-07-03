(ns status-im.multiaccounts.update.core
  (:require
   [status-im.contact.db :as contact.db]
   [status-im.contact.device-info :as device-info]
   [status-im.data-store.multiaccounts :as multiaccounts-store]
   [status-im.data-store.transport :as transport-store]
   [status-im.transport.message.protocol :as protocol]
   [status-im.transport.message.contact :as message.contact]
   [status-im.utils.fx :as fx]))

(fx/defn multiaccount-update-message [{:keys [db] :as cofx}]
  (let [multiaccount (:multiaccount db)
        fcm-token (get-in db [:notifications :fcm-token])
        {:keys [name preferred-name photo-path address]} multiaccount]
    (message.contact/ContactUpdate. (or preferred-name name) photo-path address fcm-token (device-info/all cofx))))

(fx/defn send-multiaccount-update [cofx]
  (protocol/send
   (multiaccount-update-message cofx)
   nil
   cofx))

(fx/defn send-contact-update-fx
  [{:keys [db] :as cofx} chat-id payload]
  (when-let [chat (get-in cofx [:db :transport/chats chat-id])]
    (let [updated-chat  (assoc chat :resend? "contact-update")
          tx            [(transport-store/save-transport-tx {:chat-id chat-id
                                                             :chat    updated-chat})]
          success-event [:transport/contact-message-sent chat-id]]
      (fx/merge cofx
                {:db (assoc-in db
                               [:transport/chats chat-id :resend?]
                               "contact-update")
                 :data-store/tx tx}
                (protocol/send-with-pubkey {:chat-id       chat-id
                                            :payload       payload
                                            :success-event success-event})))))

(fx/defn contact-public-keys [{:keys [db]}]
  (reduce (fn [acc [_ {:keys [public-key] :as contact}]]
            (if (contact.db/active? contact)
              (conj acc public-key)
              acc))
          #{}
          (:contacts/contacts db)))

(fx/defn send-contact-update [cofx payload]
  (let [public-keys (contact-public-keys cofx)]
    ;;NOTE: chats with contacts use public-key as chat-id
    (map #(send-contact-update-fx % payload) public-keys)))

(fx/defn multiaccount-update
  "Takes effects (containing :db) + new multiaccount fields, adds all effects necessary for multiaccount update.
  Optionally, one can specify a success-event to be dispatched after fields are persisted."
  [{:keys [db] :as cofx} new-multiaccount-fields {:keys [success-event]}]
  (let [current-multiaccount (:multiaccount db)
        new-multiaccount     (merge current-multiaccount new-multiaccount-fields)
        fx              {:db                 (assoc db :multiaccount new-multiaccount)
                         :data-store/base-tx [{:transaction (multiaccounts-store/save-multiaccount-tx new-multiaccount)
                                               :success-event success-event}]}
        {:keys [name photo-path]} new-multiaccount-fields]
    (if (or name photo-path)
      (fx/merge cofx
                fx
                #(send-multiaccount-update %))
      fx)))

(fx/defn clean-seed-phrase
  "A helper function that removes seed phrase from storage."
  [cofx]
  (multiaccount-update cofx
                       {:seed-backed-up? true
                        :mnemonic        nil}
                       {}))

(fx/defn update-sign-in-time
  [{db :db now :now :as cofx}]
  (multiaccount-update cofx {:last-sign-in now} {}))

(fx/defn update-settings
  [{{:keys [multiaccount] :as db} :db :as cofx} settings {:keys [success-event]}]
  (let [new-multiaccount (assoc multiaccount :settings settings)]
    {:db                 (assoc db :multiaccount new-multiaccount)
     :data-store/base-tx [{:transaction   (multiaccounts-store/save-multiaccount-tx new-multiaccount)
                           :success-event success-event}]}))
