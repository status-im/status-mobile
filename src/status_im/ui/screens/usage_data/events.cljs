(ns status-im.ui.screens.usage-data.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.accounts.utils :as accounts.utils]))

(handlers/register-handler-fx
  :help-improve-handler
  (fn [{db :db} [_ yes? next]]
    (merge (accounts.utils/account-update {:sharing-usage-data? yes?} {:db db})
           {:dispatch (or next [:navigate-to-clean :home])})))
