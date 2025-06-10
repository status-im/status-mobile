(ns status-im.ui.market.subs
  (:require
    [clojure.string :as string]
    [re-frame.core :as rf]
    [status-im.contexts.wallet.common.utils :as wallet.utils]
    [utils.number]
    [utils.string]))

(rf/reg-sub
 :ui.market/leaderboard
 :<- [:domain.market/leaderboard]
 (fn [leaderboard]
   (->> leaderboard
        (sort-by val)
        (map key))))

(rf/reg-sub
 :ui.market/leaderboard-loading?
 :<- [:ui.market/leaderboard]
 (fn [leaderboard]
   (empty? leaderboard)))

(rf/reg-sub
 :ui.market/token
 :<- [:domain.market/prices]
 :<- [:domain.market/tokens]
 :<- [:domain.market/leaderboard]
 (fn [[token-prices token-details leaderboard] [_ token-id]]
   (let [{:keys [current-price price-change-percentage-24h market-cap]} (get token-prices token-id)
         {short-name :symbol name :name image :image}                   (get token-details token-id)
         position                                                       (get leaderboard token-id)]
     {:price             (wallet.utils/prettify-balance "$" current-price)
      :percentage-change (utils.number/naive-round price-change-percentage-24h 2)
      :market-cap        (utils.number/format-number-with-si-suffix market-cap)
      :short-name        (when short-name (string/upper-case short-name))
      :token-rank        position
      :token-name        (utils.string/truncate name 18)
      :image             image})))
