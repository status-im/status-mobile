(ns status-im.wallet.prices
  (:require [clojure.set :as set]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.tokens :as tokens]
            [status-im.utils.currency :as currency]
            [utils.re-frame :as rf]
            [status-im.utils.prices :as prices]
            [status-im.wallet.utils :as wallet.utils]
            [taoensso.timbre :as log]))

(defn assoc-error-message
  [db error-type err]
  (assoc-in db [:wallet :errors error-type] (or err :unknown-error)))

(defn clear-error-message
  [db error-type]
  (update-in db [:wallet :errors] dissoc error-type))

(defn tokens-symbols
  [visible-token-symbols all-tokens]
  (set/difference (set visible-token-symbols)
                  (set (map :symbol (tokens/nfts-for all-tokens)))))

(re-frame/reg-fx
 :wallet/get-prices
 (fn [{:keys [from to mainnet? success-event error-event]}]
   (prices/get-prices from
                      to
                      mainnet?
                      #(re-frame/dispatch [success-event %])
                      #(re-frame/dispatch [error-event %]))))

(rf/defn on-update-prices-success
  {:events [::update-prices-success]}
  [{:keys [db]} prices]
  {:db (assoc db
              :prices          prices
              :prices-loading? false)})

(rf/defn on-update-prices-fail
  {:events [::update-prices-fail]}
  [{:keys [db]} err]
  (log/debug "Unable to get prices: " err)
  {:db (-> db
           (assoc-error-message :prices-update :error-unable-to-get-prices)
           (assoc :prices-loading? false))})

(rf/defn update-prices
  {:events [:wallet.ui/pull-to-refresh]}
  [{{:keys [network-status :wallet/all-tokens]
     {:keys [currency :wallet/visible-tokens]
      :or   {currency :usd}}
     :profile/profile
     :as db}
    :db}]
  (let [chain    (ethereum/chain-keyword db)
        mainnet? (= :mainnet chain)
        assets   (get visible-tokens chain #{})
        tokens   (tokens-symbols assets all-tokens)
        currency (get currency/currencies currency)]
    (when (not= network-status :offline)
      {:wallet/get-prices
       {:from          (if mainnet?
                         (conj tokens "ETH")
                         [(-> (tokens/native-currency (ethereum/get-current-network db))
                              (wallet.utils/exchange-symbol))])
        :to            [(:code currency)]
        :mainnet?      mainnet?
        :success-event ::update-prices-success
        :error-event   ::update-prices-fail}

       :db
       (-> db
           (clear-error-message :prices-update)
           (assoc :prices-loading? true))})))
