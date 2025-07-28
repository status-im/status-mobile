(ns status-im.domain.market.events
  (:require
    [utils.re-frame :as rf]))

(rf/reg-event-fx :domain.market/update-leaderboard
 (fn [{:keys [db]} [leaderboard]]
   {:db (update db :domain.market/leaderboard merge leaderboard)}))

(rf/reg-event-fx :domain.market/update-prices
 (fn [{:keys [db]} [new-prices]]
   (let [old-prices    (:domain.market/prices db)
         merged-prices (merge-with merge old-prices new-prices)]
     {:db (assoc db :domain.market/prices merged-prices)})))

(rf/reg-event-fx :domain.market/update-tokens
 (fn [{:keys [db]} [tokens]]
   {:db (update db :domain.market/tokens merge tokens)}))

(rf/reg-event-fx :domain.market/clear-market-data
 (fn [{:keys [db]}]
   {:db (dissoc db
         :domain.market/tokens
         :domain.market/leaderboard
         :domain.market/prices)}))
