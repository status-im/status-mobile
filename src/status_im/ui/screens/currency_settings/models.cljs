(ns status-im.ui.screens.currency-settings.models
  (:require [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.utils.fx :as fx]
            [status-im.wallet.core :as wallet]))

(fx/defn set-currency
  [{:keys [db] :as cofx} currency]
  (let [settings     (get-in db [:multiaccount :settings])
        new-settings (assoc-in settings [:wallet :currency] currency)]
    (fx/merge cofx
              (multiaccounts.update/update-settings new-settings {})
              (wallet/update-prices))))
