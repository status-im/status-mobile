(ns status-im.chat.commands.impl.transactions.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :get-collectible-token
 :<- [:collectibles]
 (fn [collectibles [_ {:keys [symbol token]}]]
   (get-in collectibles [(keyword symbol) (js/parseInt token)])))
