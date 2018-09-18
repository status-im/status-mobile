(ns status-im.accounts.update.core
  (:require [status-im.data-store.accounts :as accounts-store]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.contact :as message.contact]
            [status-im.utils.handlers-macro :as handlers-macro]))

(defn account-update
  "Takes effects (containing :db) + new account fields, adds all effects necessary for account update.
  Optionally, one can specify a success-event to be dispatched after fields are persisted."
  ([new-account-fields cofx]
   (account-update new-account-fields nil cofx))
  ([new-account-fields success-event {:keys [db] :as cofx}]
   (let [current-account (:account/account db)
         new-account     (merge current-account new-account-fields)
         fcm-token       (get-in db [:notifications :fcm-token])
         fx              {:db                 (assoc db :account/account new-account)
                          :data-store/base-tx [{:transaction (accounts-store/save-account-tx new-account)
                                                :success-event success-event}]}
         {:keys [name photo-path address]} new-account]
     (if (or (:name new-account-fields) (:photo-path new-account-fields))
       (handlers-macro/merge-fx
        cofx
        fx
        (transport/send (message.contact/ContactUpdate. name photo-path address fcm-token) nil))
       fx))))

(defn clean-seed-phrase
  "A helper function that removes seed phrase from storage."
  [cofx]
  (account-update {:seed-backed-up? true
                   :mnemonic        nil}
                  cofx))

(defn  update-sign-in-time
  [{db :db now :now :as cofx}]
  (account-update {:last-sign-in now} cofx))

(defn update-settings
  ([settings cofx] (update-settings settings nil cofx))
  ([settings success-event {{:keys [account/account] :as db} :db :as cofx}]
   (let [new-account (assoc account :settings settings)]
     {:db                 (assoc db :account/account new-account)
      :data-store/base-tx [{:transaction   (accounts-store/save-account-tx new-account)
                            :success-event success-event}]})))
