(ns status-im2.subs.wallet.collectibles
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :wallet/collectibles
 :<- [:wallet]
 (fn [wallet]
   (:collectibles wallet)))
