(ns status-im.app.market.events
  (:require
    [utils.re-frame :as rf]))

(rf/reg-event-fx :app.market/load-leaderboard
 (fn [{:keys [_db]}]
   {:fx [[:dispatch [:gate.market/start-fetching-tokens]]]}))

(rf/reg-event-fx :app.market/load-more-leaderboard-tokens
 (fn [{:keys [_db]}]
   {:fx [[:dispatch [:gate.market/fetch-more-tokens]]]}))

(rf/reg-event-fx :app.market/ensure-tokens-updating
 (fn [{:keys [db]} [first-token-to-update tokens-amount]]
   (let [token-position (get-in db [:domain.market/leaderboard first-token-to-update])]
     {:fx [[:dispatch [:gate.market/ensure-tokens-updating token-position tokens-amount]]]})))

(rf/reg-event-fx :app.market/close-leaderboard
 (fn [{:keys [_db]}]
   {:fx [[:dispatch [:gate.market/stop-fetching-tokens]]
         [:dispatch [:domain.market/clear-market-data]]]}))

(rf/reg-event-fx :app.market/reload-leaderboard
 (fn [{:keys [_db]}]
   {:fx [[:dispatch [:app.market/close-leaderboard]]
         [:dispatch [:app.market/load-leaderboard]]]}))

