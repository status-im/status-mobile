(ns status-im.ui.screens.wallet.subs
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]))

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
                  (fn [[_ fsym decimals tsym]]
                    [(re-frame/subscribe [:balance])
                     (re-frame/subscribe [:price fsym tsym])
                     (re-frame/subscribe [:wallet/currency])])
                  (fn [[balance price currency] [_ fsym decimals tsym]]
                    (when (and balance price)
                      (-> (money/internal->formatted (get balance fsym) fsym decimals)
                          (money/crypto->fiat price)
                          (money/with-precision 2)
                          str
                          (i18n/format-currency (:code currency))))))

(defn- get-balance-total-value [balance prices currency token->decimals]
  (reduce-kv (fn [acc symbol value]
               (if-let [price (get-in prices [symbol currency :price])]
                 (+ acc (-> (money/internal->formatted value symbol (token->decimals symbol))
                            (money/crypto->fiat price)
                            .toNumber))
                 acc)) 0 balance))

(re-frame/reg-sub :wallet/all-tokens
                  (fn [db] (:wallet/all-tokens db)))

(re-frame/reg-sub :portfolio-value
                  :<- [:balance]
                  :<- [:prices]
                  :<- [:wallet/currency]
                  :<- [:network]
                  :<- [:wallet/all-tokens]
                  (fn [[balance prices currency network all-tokens] [_ currency-code]]
                    (if (and balance prices)
                      (let [assets          (tokens/tokens-for all-tokens (ethereum/network->chain-keyword network))
                            token->decimals (into {} (map #(vector (:symbol %) (:decimals %)) assets))
                            balance-total-value
                            (get-balance-total-value balance
                                                     prices
                                                     (or currency-code
                                                         (-> currency :code keyword))
                                                     token->decimals)]
                        (if (pos? balance-total-value)
                          (-> balance-total-value
                              (money/with-precision 2)
                              str
                              (i18n/format-currency (:code currency) false))
                          "0"))
                      "...")))

(re-frame/reg-sub :prices-loading?
                  (fn [db]
                    (:prices-loading? db)))

(re-frame/reg-sub :wallet/balance-loading?
                  :<- [:wallet]
                  (fn [wallet]
                    (:balance-loading? wallet)))

(re-frame/reg-sub :wallet/error-message
                  :<- [:wallet]
                  (fn [wallet]
                    (or (get-in wallet [:errors :balance-update])
                        (get-in wallet [:errors :prices-update]))))

(re-frame/reg-sub :get-wallet-unread-messages-number
                  (fn [db]
                    0))

(re-frame/reg-sub :wallet/visible-tokens-symbols
                  :<- [:network]
                  :<- [:account/account]
                  (fn [[network current-account]]
                    (let [chain (ethereum/network->chain-keyword network)]
                      (get-in current-account [:settings :wallet :visible-tokens chain]))))

(re-frame/reg-sub :wallet/visible-assets
                  :<- [:network]
                  :<- [:wallet/visible-tokens-symbols]
                  :<- [:wallet/all-tokens]
                  (fn [[network visible-tokens-symbols all-tokens]]
                    (let [chain (ethereum/network->chain-keyword network)]
                      (conj (filter #(contains? visible-tokens-symbols (:symbol %))
                                    (tokens/sorted-tokens-for all-tokens (ethereum/network->chain-keyword network)))
                            (tokens/native-currency chain)))))

(re-frame/reg-sub :wallet/visible-assets-with-amount
                  :<- [:balance]
                  :<- [:wallet/visible-assets]
                  (fn [[balance visible-assets]]
                    (map #(assoc % :amount (get balance (:symbol %))) visible-assets)))

(re-frame/reg-sub :wallet/transferrable-assets-with-amount
                  :<- [:wallet/visible-assets-with-amount]
                  (fn [all-assets]
                    (filter #(not (:nft? %)) all-assets)))

(re-frame/reg-sub :wallet/currency
                  :<- [:wallet.settings/currency]
                  (fn [currency-id]
                    (get constants/currencies currency-id)))
