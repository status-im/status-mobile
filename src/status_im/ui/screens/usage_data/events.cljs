(ns status-im.ui.screens.usage-data.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.accounts.events :as accounts]))

(handlers/register-handler-fx
  :help-improve-handler
  (fn [{{:accounts/keys [accounts current-account-id] :as db} :db} [_ yes? address next]]
    (let [{:keys [sharing-usage-data?]} (get accounts current-account-id)]
      (merge (accounts/account-update {:sharing-usage-data? yes?} {:db db})
             {:dispatch-n [(if yes?
                             [:register-mixpanel-tracking address]
                             (when (and next sharing-usage-data?)
                               [:unregister-mixpanel-tracking]))
                           (or next [:navigate-to-clean :home])]}))))

