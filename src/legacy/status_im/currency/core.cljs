(ns legacy.status-im.currency.core
  (:require
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [legacy.status-im.wallet.prices :as prices]
    [utils.re-frame :as rf]))

(defn get-currency
  [db]
  (get-in db [:profile/profile :currency] :usd))

(rf/defn set-currency
  {:events [:wallet-legacy.settings.ui/currency-selected]}
  [{:keys [db] :as cofx} currency]
  (rf/merge cofx
            (multiaccounts.update/multiaccount-update
             :currency
             currency
             ;; on changing currency, we should fetch tokens prices again
             {:on-success #(rf/dispatch [:wallet/get-wallet-token])})
            (prices/update-prices)))
