(ns status-im.ui.screens.currency-settings.models
  (:require [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.utils.fx :as fx]
            [status-im.wallet.prices :as prices]))

(defn get-currency [db]
  (get-in db [:multiaccount :currency] :usd))

(fx/defn set-currency
  [{:keys [db] :as cofx} currency]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update
             :currency currency
             {})
            (prices/update-prices)))
