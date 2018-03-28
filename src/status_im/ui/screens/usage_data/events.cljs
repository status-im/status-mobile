(ns status-im.ui.screens.usage-data.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.accounts.events :as accounts]))

(handlers/register-handler-fx
  :help-improve-handler
  (fn [{db :db} [_ yes? address next]]
    (merge (accounts/account-update {:db db} {:sharing-usage-data? yes?})
           {:dispatch-n [(if yes?
                           [:register-mixpanel-tracking address]
                           [:unregister-mixpanel-tracking])
                         next]})))

