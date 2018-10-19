(ns status-im.ui.screens.wallet.collectibles.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub :collectibles :collectibles)

(re-frame/reg-sub
 :screen-collectibles
 :<- [:collectibles]
 :<- [:get-screen-params]
 (fn [[collectibles {:keys [symbol]}]]
   (when-let [v (get collectibles symbol)]
     (mapv #(assoc (second %) :id (first %)) v))))
