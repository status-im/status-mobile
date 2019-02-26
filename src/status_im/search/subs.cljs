(ns status-im.search.subs
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :search/filter
 (fn [db]
   (get-in db [:ui/search :filter])))

(defn extract-browser-attributes
  [browser]
  (let [{:keys [browser-id name]} (val browser)]
    [browser-id name]))

(defn extract-chat-attributes [chat]
  (let [{:keys [name random-name tags]} (val chat)]
    (into [name random-name] tags)))

(defn apply-filter
  "extract-attributes-fn is a function that take an element from the collection
  and returns a vector of attributes which are strings
  apply-filter returns the elements for which at least one attribute includes
  the search-filter
  apply-filter returns nil if the search-filter is empty or if there is no element
  that match the filter"
  [search-filter coll extract-attributes-fn]
  (when (not-empty search-filter)
    (let [search-filter (string/lower-case search-filter)
          results (filter (fn [element]
                            (some (fn [s]
                                    (when (string? s)
                                      (string/includes? (string/lower-case s)
                                                        search-filter)))
                                  (extract-attributes-fn element)))
                          coll)]
      (when (not-empty results)
        (sort-by #(-> % second :timestamp) >
                 (into {} results))))))

(re-frame/reg-sub
 :search/filtered-chats
 :<- [:chats/active-chats]
 :<- [:search/filter]
 (fn [[chats search-filter]]
   (apply-filter search-filter chats extract-chat-attributes)))

(re-frame/reg-sub
 :search/filtered-browsers
 :<- [:browser/browsers]
 :<- [:search/filter]
 (fn [[browsers search-filter]]
   (apply-filter search-filter browsers extract-browser-attributes)))
