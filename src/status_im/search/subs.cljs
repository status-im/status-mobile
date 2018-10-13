(ns status-im.search.subs
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :search/filter
 (fn [db]
   (get-in db [:ui/search :filter] "")))

(re-frame/reg-sub
 :search/filtered-active-chats
 :<- [:get-active-chats]
 :<- [:search/filter]
 (fn [[chats tag-filter]]
   (if (empty? tag-filter)
     chats
     (keep #(when (some (fn [tag]
                          (string/includes? (string/lower-case tag)
                                            (string/lower-case tag-filter)))
                        (into [(:name (val %))] (:tags (val %))))
              %)
           chats))))

(re-frame/reg-sub
 :search/filtered-home-items
 :<- [:search/filtered-active-chats]
 (fn [active-chats]
   active-chats))
