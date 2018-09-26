(ns status-im.accounts.update.core
  (:require [status-im.data-store.accounts :as accounts-store]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.core :as v1]
            [status-im.utils.fx :as fx]))

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
                #(transport/send (v1/ContactUpdate. name photo-path address fcm-token) nil %))
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
