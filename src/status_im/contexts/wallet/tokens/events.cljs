(ns status-im.contexts.wallet.tokens.events
  (:require [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.validation :as wallet-validation]
            [status-im.contexts.wallet.tokens.data :as tokens-data]
            [status-im.contexts.wallet.tokens.effects]
            [taoensso.timbre :as log]
            [utils.ethereum.chain :as chain]))

(rf/reg-event-fx
 :wallet.tokens/get-token-list
 (fn [{:keys [db]}]
   {:db (assoc-in db [:wallet :ui :loading :token-list] true)
    :fx [[:json-rpc/call
          [{:method     "wallet_getTokenList"
            :params     []
            :on-success [:wallet.tokens/store-token-list]
            :on-error   [:wallet.tokens/get-token-list-failed]}]]]}))

(rf/reg-event-fx
 :wallet.tokens/store-token-list
 (fn [{:keys [db]} [{:keys [data]}]]
   (let [chain-ids (chain/chain-ids db)
         tokens    (reduce (fn [{:keys [by-address by-symbol] :as data}
                                {:keys [name source version tokens]}]
                             (-> data
                                 (update :sources
                                         conj
                                         {:name         name
                                          :source       source
                                          :version      version
                                          :tokens-count (count tokens)})
                                 (update :by-address
                                         merge
                                         (tokens-data/tokens-by-address
                                          {:added-tokens by-address
                                           :source-name  name
                                           :tokens       tokens
                                           :chain-ids    chain-ids}))
                                 (update :by-symbol
                                         merge
                                         (tokens-data/tokens-by-symbol
                                          {:added-tokens by-symbol
                                           :source-name  name
                                           :tokens       tokens
                                           :chain-ids    chain-ids}))))
                           {:sources    []
                            :by-address {}
                            :by-symbol  {}}
                           data)
         symbols   (->> tokens
                        :by-symbol
                        keys
                        (remove wallet-validation/eth-address?))]
     {:fx [[:effects.wallet.tokens/fetch-market-values
            {:symbols    symbols
             :currency   constants/profile-default-currency
             :on-success #(rf/dispatch [:wallet.tokens/store-market-values %])
             :on-error   #(rf/dispatch [:wallet.tokens/fetch-market-values-failed %])}]
           [:effects.wallet.tokens/fetch-details
            {:symbols    symbols
             :on-success #(rf/dispatch [:wallet.tokens/store-details %])
             :on-error   #(rf/dispatch [:wallet.tokens/fetch-details-failed %])}]
           [:effects.wallet.tokens/fetch-prices
            {:symbols    symbols
             :currencies [constants/profile-default-currency]
             :on-success #(rf/dispatch [:wallet.tokens/store-prices %])
             :on-error   #(rf/dispatch [:wallet.tokens/fetch-prices-failed %])}]]
      :db (-> db
              (assoc-in [:wallet :tokens]
                        {:sources    (:sources tokens)
                         :by-address (-> tokens :by-address vals)
                         :by-symbol  (-> tokens :by-symbol vals)})
              (assoc-in [:wallet :ui :loading]
                        {:token-list    false
                         :market-values true
                         :details       true
                         :prices        true}))})))

(rf/reg-event-fx
 :wallet.tokens/get-token-list-failed
 (fn [{:keys [db]} [error]]
   (log/info "failed to get wallet tokens "
             {:error error
              :event :wallet.tokens/get-token-list})
   {:db (assoc-in db [:wallet :ui :loading :token-list] false)}))

(rf/reg-event-fx
 :wallet.tokens/store-market-values
 (fn [{:keys [db]} [raw-data]]
   (let [market-values (reduce (fn [acc [token data]]
                                 (assoc acc token {:market-cap      (:MKTCAP data)
                                                   :high-day        (:HIGHDAY data)
                                                   :low-day         (:LOWDAY data)
                                                   :change-pct-hour (:CHANGEPCTHOUR data)
                                                   :change-pct-day  (:CHANGEPCTDAY data)
                                                   :change-pct-24h  (:CHANGEPCT24HOUR data)
                                                   :change-24h      (:CHANGE24HOUR data)}))
                               {}
                               raw-data)]
     {:db (-> db
              (assoc-in [:wallet :tokens :market-values-per-token] market-values)
              (assoc-in [:wallet :ui :loading :market-values] false))})))

(rf/reg-event-fx
 :wallet.tokens/fetch-market-values-failed
 (fn [{:keys [db]} [error]]
   (log/info "failed to get wallet market values "
             {:error error
              :event :wallet.tokens/fetch-market-values})
   {:db (assoc-in db [:wallet :ui :loading :market-values] false)}))

(rf/reg-event-fx
 :wallet.tokens/store-details
 (fn [{:keys [db]} [details]]
   {:db (-> db
            (assoc-in [:wallet :tokens :details-per-token] details)
            (assoc-in [:wallet :ui :loading :details] false))}))

(rf/reg-event-fx
 :wallet.tokens/fetch-details-failed
 (fn [{:keys [db]} [error]]
   (log/info "failed to get wallet details "
             {:error error
              :event :wallet.tokens/fetch-details})
   {:db (assoc-in db [:wallet :ui :loading :details] false)}))

(rf/reg-event-fx
 :wallet.tokens/store-prices
 (fn [{:keys [db]} [prices]]
   {:db (-> db
            (assoc-in [:wallet :tokens :prices-per-token] prices)
            (assoc-in [:wallet :ui :loading :prices] false))}))

(rf/reg-event-fx
 :wallet.tokens/fetch-prices-failed
 (fn [{:keys [db]} [error]]
   (log/info "failed to get wallet prices "
             {:error error
              :event :wallet.tokens/fetch-prices})
   {:db (assoc-in db [:wallet :ui :loading :prices] false)}))
