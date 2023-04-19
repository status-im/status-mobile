(ns status-im2.subs.home
  (:require [re-frame.core :as re-frame]))

(def memo-chats-stack-items (atom nil))

(re-frame/reg-sub
 :chats-stack-items
 :<- [:chats/home-list-chats]
 :<- [:view-id]
 :<- [:home-items-show-number]
 (fn [[chats view-id home-items-show-number]]
   (if (= view-id :chats-stack)
     (let [res (take home-items-show-number chats)]
       (reset! memo-chats-stack-items res)
       res)
     ;;we want to keep data unchanged so react doesn't change component when we leave screen
     @memo-chats-stack-items)))

(re-frame/reg-sub
 :hide-home-tooltip?
 :<- [:multiaccount]
 (fn [multiaccount]
   (:hide-home-tooltip? multiaccount)))
