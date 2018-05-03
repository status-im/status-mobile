(ns status-im.ui.screens.usage-data.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.accounts.utils :as accounts.utils]))

(handlers/register-handler-fx
  :help-improve-handler
  (fn [cofx [_ yes? next]]
    (merge (accounts.utils/account-update {:sharing-usage-data? yes?} cofx)
           {:dispatch (or next [:navigate-to-clean :home])})))
