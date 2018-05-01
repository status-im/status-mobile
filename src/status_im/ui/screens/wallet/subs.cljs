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

(re-frame/reg-sub :price
  (fn [db]
    (get-in db [:prices :price])))

(re-frame/reg-sub :last-day
  (fn [db]
    (get-in db [:prices :last-day])))

(re-frame/reg-sub :portfolio-value
  :<- [:balance]
  :<- [:price]
  (fn [[balance price]]
    (if (and balance price)
      (-> (money/wei->ether (get balance :ETH)) ;; TODO(jeluard) Modify to consider tokens
          (money/eth->usd price)
          (money/with-precision 2)
          str)
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
