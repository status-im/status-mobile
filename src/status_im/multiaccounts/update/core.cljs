(ns status-im.multiaccounts.update.core
  (:require [status-im.contact.db :as contact.db]

            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.transport.message.contact :as message.contact]
            [status-im.transport.message.protocol :as protocol]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(fx/defn multiaccount-update-message [{:keys [db] :as cofx}]
  (let [multiaccount (:multiaccount db)
        {:keys [name preferred-name photo-path address]} multiaccount]
    (message.contact/ContactUpdate. (or preferred-name name) photo-path address nil nil)))

(fx/defn send-multiaccount-update [cofx]
  (protocol/send
   (multiaccount-update-message cofx)
   nil
   cofx))

(fx/defn send-contact-update-fx
  [{:keys [db] :as cofx} chat-id payload]
  (protocol/send-with-pubkey cofx
                             {:chat-id       chat-id
                              :payload       payload
                              :success-event [:transport/contact-message-sent chat-id]}))

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
  [{:keys [db] :as cofx}
   setting setting-value
   {:keys [on-success] :or {on-success #()}}]
  (let [current-multiaccount (:multiaccount db)]
    (if (empty? current-multiaccount)
      ;; NOTE: this should never happen, but if it does this is a critical error
      ;; and it is better to crash than risk having an unstable state
      (throw (js/Error. "Please shake the phone to report this error and restart the app. multiaccount is currently empty, which means something went wrong when trying to update it with"))
      (fx/merge cofx
                {:db (if setting-value
                       (assoc-in db [:multiaccount setting] setting-value)
                       (update db :multiaccount dissoc setting))
                 ::json-rpc/call
                 [{:method "settings_saveSetting"
                   :params [setting setting-value]
                   :on-success on-success}]}
                (when (#{:name :photo-path :prefered-name} setting)
                  (send-multiaccount-update))))))

(fx/defn clean-seed-phrase
  "A helper function that removes seed phrase from storage."
  [cofx]
  (multiaccount-update cofx
                       :mnemonic nil
                       {}))
