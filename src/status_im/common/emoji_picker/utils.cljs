(ns status-im.common.emoji-picker.utils
  (:require
    [clojure.string :as string]
    [status-im.common.emoji-picker.constants :as constants]
    [status-im.common.emoji-picker.data :refer [emoji-data]]))

(defn search-emoji
  [search-query]
  (let [cleaned-query (string/lower-case (string/trim search-query))]
    (->> emoji-data
         (filter (fn [{:keys [label tags emoticon]}]
                   (or (string/includes? label cleaned-query)
                       (when emoticon
                         (if (vector? emoticon)
                           (some #(string/includes? (string/lower-case %) cleaned-query) emoticon)
                           (string/includes? (string/lower-case emoticon) cleaned-query)))
                       (some #(string/includes? % cleaned-query) tags))))
         (partition-all constants/emojis-per-row))))

(defn random-emoji
  []
  (:unicode (rand-nth emoji-data)))
