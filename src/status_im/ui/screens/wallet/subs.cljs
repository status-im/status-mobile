(ns status-im.ui.screens.wallet.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.money :as money]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]))

(re-frame/reg-sub :wallet
  (fn [db]
    (:wallet db)))

(re-frame/reg-sub :balance
  :<- [:wallet]
  (fn [wallet]
    (:balance wallet)))

(re-frame/reg-sub :prices
  (fn [db]
    (:prices db)))

(re-frame/reg-sub :price
  :<- [:prices]
  (fn [prices [_ fsym tsym]]
    (get-in prices [fsym tsym :price])))

(re-frame/reg-sub :last-day
  :<- [:prices]
  (fn [prices [_ fsym tsym]]
    (get-in prices [fsym tsym :last-day])))

(re-frame/reg-sub :asset-value
  (fn [[_ fsym tsym]]
    [(re-frame/subscribe [:balance])
     (re-frame/subscribe [:price fsym tsym])])
  (fn [[balance price] [_ fsym tsym]]
    (when (and balance price)
      (-> (money/wei->ether (get balance fsym))
          (money/eth->fiat price)
          (money/with-precision 2)
          str))))

(defn- get-balance-total-value [balance prices currency]
  (->> balance
       (reduce-kv (fn [acc symbol value]
                    (if-let [price (get-in prices [symbol currency :price])]
                      (+ acc (-> (money/wei->ether value)
                                 (money/eth->fiat price)
                                 .toNumber))
                      acc)) 0)))

(re-frame/reg-sub :portfolio-value
  :<- [:balance]
  :<- [:prices]
  (fn [[balance prices] [_ currency]]
    (if (and balance prices)
      (let [balance-total-value (get-balance-total-value balance prices currency)]
        (-> balance-total-value
            (money/with-precision 2)
            str))
      "...")))

(re-frame/reg-sub :prices-loading?
  (fn [db]
    (:prices-loading? db)))

(re-frame/reg-sub :wallet/balance-loading?
  :<- [:wallet]
  (fn [wallet]
    (:balance-loading? wallet)))

(re-frame/reg-sub :wallet/error-message?
  :<- [:wallet]
  (fn [wallet]
    (or (get-in wallet [:errors :balance-update])
        (get-in wallet [:errors :prices-update]))))

(re-frame/reg-sub :get-wallet-unread-messages-number
  (fn [db]
    0))

(re-frame/reg-sub :wallet/visible-tokens-symbols
  :<- [:network]
  :<- [:get-current-account]
  (fn [[network current-account]]
    (let [chain (ethereum/network->chain-keyword network)]
      (get-in current-account [:settings :wallet :visible-tokens chain]))))

(re-frame/reg-sub :wallet/visible-assets
  :<- [:network]
  :<- [:wallet/visible-tokens-symbols]
  (fn [[network visible-tokens-symbols]]
    (conj (filter #(contains? visible-tokens-symbols (:symbol %))
                  (tokens/tokens-for (ethereum/network->chain-keyword network)))
          tokens/ethereum)))

(re-frame/reg-sub :wallet/visible-assets-with-amount
  :<- [:balance]
  :<- [:wallet/visible-assets]
  (fn [[balance visible-assets]]
    (map #(assoc % :amount (get balance (:symbol %))) visible-assets)))
