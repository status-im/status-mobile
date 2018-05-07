(ns status-im.ui.screens.accounts.utils
  (:require [status-im.transport.message.core :as transport]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.transport.message.v1.contact :as message.contact]))

(defn account-update
  "Takes effects (containing :db) + new account fields, adds all effects necessary for account update.
  Optionally, one can specify event to be dispatched after fields are persisted."
  ([new-account-fields cofx]
   (account-update new-account-fields nil cofx))
  ([new-account-fields after-update-event {:keys [db] :as cofx}]
   (let [current-account (:account/account db)
         new-account     (merge current-account new-account-fields)
         fx              {:db                      (assoc db :account/account new-account)
                          :data-store/save-account (assoc new-account :after-update-event after-update-event)}
         {:keys [name photo-path]} new-account]
     (if (or (:name new-account-fields) (:photo-path new-account-fields))
       (handlers-macro/merge-fx cofx fx (transport/send (message.contact/ContactUpdate. name photo-path) nil))
       fx))))
