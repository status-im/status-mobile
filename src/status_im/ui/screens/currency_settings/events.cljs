(ns status-im.ui.screens.currency-settings.events
  (:require [status-im.ui.screens.accounts.events :as accounts]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
  :wallet.settings/set-currency
  (fn [{:keys [db] :as cofx} [_ currency]]
    (let [settings     (get-in db [:account/account :settings])
          new-settings (assoc-in settings [:wallet :currency] currency)]
      (accounts/update-settings new-settings cofx))))
