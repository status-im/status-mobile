(ns status-im.chats-list.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [clojure.string :as str]))

(defn search-filter [text item]
  (let [name (-> (or (:name item) "")
                 (str/lower-case))
        text (str/lower-case text)]
    (not= (str/index-of name text) nil)))

(reg-sub :filtered-chats
  :<- [:get :chats]
  :<- [:get-in [:toolbar-search :text]]
  (fn [[chats search-text]]
    (if search-text
      (filter #(search-filter search-text (second %)) chats)
      chats)))
