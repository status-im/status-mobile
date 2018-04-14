(ns status-im.ui.screens.usage-data.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.accounts.events :as accounts]))

(handlers/register-handler-fx
  :help-improve-handler
  (fn [{db :db} [_ yes? next]]
    (merge (accounts/account-update {:sharing-usage-data? yes?} {:db db})
           {:dispatch (or next [:navigate-to-clean :home])})))
