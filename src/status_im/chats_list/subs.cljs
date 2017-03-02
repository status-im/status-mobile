(ns status-im.chats-list.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub subscribe]]
            [clojure.string :as str]))

(defn search-filter [text item]
  (let [name (-> (or (:name item) "")
                 (str/lower-case))
        text (str/lower-case text)]
    (not= (str/index-of name text) nil)))

(register-sub :filtered-chats
  (fn [_ _]
    (let [chats       (subscribe [:get :chats])
          search-text (subscribe [:get-in [:toolbar-search :text]])]
      (reaction
       (if @search-text
         (filter #(search-filter @search-text (second %)) @chats)
         @chats)))))
