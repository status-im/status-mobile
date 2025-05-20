(ns status-im.domain.subs
  (:require
    [utils.re-frame :as rf]))

(rf/reg-root-key-sub :domain.market/leaderboard :domain.market/leaderboard)
(rf/reg-root-key-sub :domain.market/prices :domain.market/prices)
(rf/reg-root-key-sub :domain.market/tokens :domain.market/tokens)
