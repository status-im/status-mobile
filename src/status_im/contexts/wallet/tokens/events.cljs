(ns status-im.contexts.wallet.tokens.events
  (:require [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.tokens.data :as tokens-data]
            [status-im.contexts.wallet.tokens.effects]
            [taoensso.timbre :as log]
            [utils.address]))

;; NOTE: fetching the token list and the rest of the data (market-values, token-details, token-prices)
;; separately, since getting the token list is about 10x quicker.

(rf/reg-event-fx :wallet.tokens/get-token-list
 (fn [{:keys [db]}]
   (when-not (-> db :wallet :tokens)
     {:fx [[:effects.wallet.tokens/fetch-token-list
            {:on-success [:wallet.tokens/store-token-list]
             :on-error   [:wallet.tokens/get-token-list-failed]}]]})))

(rf/reg-event-fx :wallet.tokens/store-token-list
 (fn [{:keys [db]} [{:keys [sources tokens-by-symbol symbol-lists]}]]
   (let [all-symbols (tokens-data/all-token-symbols symbol-lists)
         currency    (or (-> db :profile/profile :currency)
                         constants/profile-default-currency)]
     {:db (assoc-in db
           [:wallet :tokens]
           {:sources          sources
            :symbol-lists     symbol-lists
            :tokens-by-symbol tokens-by-symbol})
      :fx [[:effects.wallet.tokens/fetch-additional-token-data
            {:currency   currency
             :symbols    all-symbols
             :on-success [:wallet.tokens/store-additional-token-data]
             :on-error   [:wallet.tokens/get-token-list-failed]}]]})))

(rf/reg-event-fx :wallet.tokens/store-additional-token-data
 (fn [{:keys [db]} [{:keys [market-values prices details]}]]
   {:db (update-in db
                   [:wallet :tokens]
                   assoc
                   :market-values-by-symbol market-values
                   :prices-by-symbol        prices
                   :details-by-symbol       details)}))

(rf/reg-event-fx :wallet.tokens/get-token-list-failed
 (fn [_ [error]]
   (log/info "failed to get wallet tokens"
             {:error error
              :event :wallet.tokens/get-token-list-failed})))

(rf/reg-event-fx :wallet.tokens/get-additional-token-data-failed
 (fn [_ [error]]
   (log/info "failed to get wallet token data"
             {:error error
              :event :wallet.tokens/get-additional-token-data-failed})))
