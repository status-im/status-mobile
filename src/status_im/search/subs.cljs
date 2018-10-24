(ns status-im.search.subs
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :search/filter
 (fn [db]
   (get-in db [:ui/search :filter] "")))

(defn filter-chats
  [[chats search-filter]]
  (if (empty? search-filter)
    chats
    (let [search-filter (string/lower-case search-filter)]
      (keep #(let [{:keys [name random-name tags]} (val %)]
               (when (some (fn [s]
                             (when s
                               (string/includes? (string/lower-case s)
                                                 search-filter)))
                           (into [name random-name] tags))
                 %))
            chats))))

(re-frame/reg-sub
 :search/filtered-active-chats
 :<- [:get-active-chats]
 :<- [:search/filter]
 filter-chats)

(re-frame/reg-sub
 :search/filtered-home-items
 :<- [:search/filtered-active-chats]
 (fn [active-chats]
   (sort-by #(-> % second :timestamp) > active-chats)))
