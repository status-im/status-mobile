(ns status-im2.contexts.emoji-picker.utils
  (:require [clojure.string :as string]
            [status-im2.contexts.emoji-picker.constants :as constants]
            [status-im2.contexts.emoji-picker.data :refer [emoji-data]]))

(defn search-emoji
  [search-query]
  (let [cleaned-query (string/lower-case (string/trim search-query))]
    (->> (filter (fn [{:keys [label tags emoticon]}]
                   (or (string/includes? label cleaned-query)
                       (when emoticon
                         (if (vector? emoticon)
                           (some #(string/includes? (string/lower-case %) cleaned-query) emoticon)
                           (string/includes? (string/lower-case emoticon) cleaned-query)))
                       (some #(string/includes? % cleaned-query) tags)))
                 emoji-data)
         (partition-all constants/emojis-per-row))))
