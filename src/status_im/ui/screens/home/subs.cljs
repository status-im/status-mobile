(ns status-im.ui.screens.home.subs
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
  :<- [:get-in [:toolbar-search :show]]
  (fn [[chats search-text show-search]]
    (let [unordered-chats (if (and (= show-search :home) search-text)
                            (filter #(search-filter search-text (second %))
                                    chats)
                            chats)]
      (sort-by #(-> % second :timestamp) > unordered-chats))))
