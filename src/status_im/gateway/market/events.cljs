(ns status-im.gateway.market.events
  (:require
    [status-im.gateway.market.core :as core]
    [status-im.gateway.market.transform :as t]
    [utils.number]
    [utils.re-frame :as rf]))

(def ^:const leaderboard-request-page-size 20)

(rf/reg-event-fx :gate.market/start-fetching-tokens
 (fn [{:keys [db]}]
   (let [start-page 1]
     {:db (assoc db :gate.market/last-fetched-page start-page)
      :fx [[:dispatch [:gate.market/fetch-token-page start-page leaderboard-request-page-size]]]})))

(rf/reg-event-fx :gate.market/fetch-more-tokens
 (fn [{:keys [db]}]
   (let [last-page               (:gate.market/last-fetched-page db)
         next-page               (inc last-page)
         should-fetch-next-page? (> last-page 0)]
     (when should-fetch-next-page?
       {:db (assoc db :gate.market/last-fetched-page next-page)
        :fx [[:dispatch [:gate.market/fetch-token-page next-page leaderboard-request-page-size]]]}))))

(rf/reg-event-fx :gate.market/ensure-tokens-updating
 (fn [_ [token-position tokens-amount]]
   (let [{:keys [page-size page-index]} (core/page-size-to-ensure-tokens-updating
                                         {:first-visible-index token-position
                                          :visible-count       tokens-amount})]
     {:fx [[:dispatch [:gate.market/fetch-token-page page-index page-size]]]})))

(rf/reg-event-fx :gate.market/fetch-token-page
 (fn [_ [page page-size]]
   (let [page       page
         sort-order 0
         currency   ""
         params     [page
                     page-size
                     sort-order
                     currency]]
     {:fx [[:dispatch
            [:gate.rpc/call
             {:method "wallet_fetchMarketTokenPageAsync"
              :params params}]]]})))

(rf/reg-event-fx :gate.market/stop-fetching-tokens
 (fn [{:keys [db]}]
   {:db (assoc db :gate.market/last-fetched-page 0)
    :fx [[:dispatch
          [:gate.rpc/call
           {:method "wallet_unsubscribeFromLeaderboard"}]]]}))

(rf/reg-event-fx :gate.market/leaderboard-page-fetched
 (fn [_ [payload]]
   (let [message (t/wallet-signal-message->clj payload)]
     {:fx [[:dispatch [:gate.market/tranform-and-save-page-data message]]]})))

(rf/reg-event-fx :gate.market/leaderboard-page-data-updated
 (fn [_ [payload]]
   (let [message (t/wallet-signal-message->clj payload)]
     {:fx [[:dispatch [:gate.market/tranform-and-save-page-data message]]]})))

(rf/reg-event-fx :gate.market/tranform-and-save-page-data
 (fn [_ [message]]
   (let [leaderboard (t/leaderboard-message->positions-by-token message)
         prices      (t/leaderboard-message->prices-by-token-id message)
         tokens      (t/leaderboard-message->token-data-by-token-id message)]
     {:fx [[:dispatch [:domain.market/update-leaderboard leaderboard]]
           [:dispatch [:domain.market/update-prices prices]]
           [:dispatch [:domain.market/update-tokens tokens]]]})))

(rf/reg-event-fx :gate.market/leaderboard-page-prices-updated
 (fn [_ [payload]]
   (let [message       (t/wallet-signal-message->clj payload)
         prices-update (t/price-update-message->price-updates-by-token-id message)]
     {:fx [[:dispatch [:domain.market/update-prices prices-update]]]})))
