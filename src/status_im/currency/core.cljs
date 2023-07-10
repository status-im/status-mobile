(ns status-im.currency.core
  (:require [status-im.multiaccounts.update.core :as multiaccounts.update]
            [utils.re-frame :as rf]
            [status-im.wallet.prices :as prices]))

(defn get-currency
  [db]
  (get-in db [:profile/profile :currency] :usd))

(rf/defn set-currency
  {:events [:wallet.settings.ui/currency-selected]}
  [{:keys [db] :as cofx} currency]
  (rf/merge cofx
            (multiaccounts.update/multiaccount-update
             :currency
             currency
             {})
            (prices/update-prices)))
