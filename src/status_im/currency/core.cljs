(ns status-im.currency.core
  (:require
    [status-im.multiaccounts.update.core :as multiaccounts.update]
    [status-im.wallet.prices :as prices]
    [utils.re-frame :as rf]))

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
