(ns status-im.accounts.update.core
  (:require [status-im.data-store.accounts :as accounts-store]
            [status-im.transport.message.protocol :as protocol]
            [status-im.data-store.transport :as transport-store]
            [status-im.transport.message.contact :as message.contact]
            [status-im.utils.fx :as fx]))

(fx/defn account-update-message [{:keys [db]}]
  (let [account (:account/account db)
        fcm-token (get-in db [:notifications :fcm-token])
        {:keys [name photo-path address]} account]
    (message.contact/ContactUpdate. name photo-path address fcm-token)))

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
  (reduce (fn [acc [_ {:keys [public-key dapp? pending?]}]]
            (if (and (not dapp?)
                     (not pending?))
              (conj acc public-key)
              acc))
          #{}
          (:contacts/contacts db)))

(fx/defn send-contact-update [cofx payload]
  (let [public-keys (contact-public-keys cofx)]
    ;;NOTE: chats with contacts use public-key as chat-id
    (map #(send-contact-update-fx % payload) public-keys)))

(fx/defn account-update
  "Takes effects (containing :db) + new account fields, adds all effects necessary for account update.
  Optionally, one can specify a success-event to be dispatched after fields are persisted."
  [{:keys [db] :as cofx} new-account-fields {:keys [success-event]}]
  (let [current-account (:account/account db)
        new-account     (merge current-account new-account-fields)
        fcm-token       (get-in db [:notifications :fcm-token])
        fx              {:db                 (assoc db :account/account new-account)
                         :data-store/base-tx [{:transaction (accounts-store/save-account-tx new-account)
                                               :success-event success-event}]}
        {:keys [name photo-path address]} new-account]
    (if (or (:name new-account-fields) (:photo-path new-account-fields))
      (fx/merge cofx
                fx
                #(protocol/send (account-update-message %) nil %))
      fx)))

(fx/defn clean-seed-phrase
  "A helper function that removes seed phrase from storage."
  [cofx]
  (account-update cofx
                  {:seed-backed-up? true
                   :mnemonic        nil}
                  {}))

(fx/defn update-sign-in-time
  [{db :db now :now :as cofx}]
  (account-update cofx {:last-sign-in now} {}))

(fx/defn update-settings
  [{{:keys [account/account] :as db} :db :as cofx} settings {:keys [success-event]}]
  (let [new-account (assoc account :settings settings)]
    {:db                 (assoc db :account/account new-account)
     :data-store/base-tx [{:transaction   (accounts-store/save-account-tx new-account)
                           :success-event success-event}]}))
