(ns status-im.ui.screens.currency-settings.models
  (:require [status-im.accounts.update.core :as accounts.update]
            [status-im.utils.fx :as fx]
            [status-im.wallet.core :as wallet]))

(fx/defn set-currency
  [{:keys [db] :as cofx} currency]
  (let [settings     (get-in db [:account/account :settings])
        new-settings (assoc-in settings [:wallet :currency] currency)]
    (fx/merge cofx
              (accounts.update/update-settings new-settings {})
              (wallet/update-prices))))
