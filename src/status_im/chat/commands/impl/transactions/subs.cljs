(ns status-im.chat.commands.impl.transactions.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :get-collectible-token
 :<- [:collectibles]
 (fn [collectibles [_ nft-symbol token-id]]
   (get-in collectibles [(keyword nft-symbol) (js/parseInt token-id)])))
