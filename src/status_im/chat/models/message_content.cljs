(ns status-im.chat.models.message-content
  (:require [clojure.string :as string]
            [status-im.utils.platform :as platform]
            [status-im.constants :as constants]))

(def stylings [[:bold   constants/regx-bold]
               [:italic constants/regx-italic]
               [:backquote constants/regx-backquote]])

(def ^:private actions [[:link    constants/regx-url]
                        [:tag     constants/regx-tag]
                        [:mention constants/regx-mention]])

(def blank " ")

(defn- blank-string [size]
  (.repeat ^js blank size))

(defn should-collapse? [text line-count]
  (or (<= constants/chars-collapse-threshold (count text))
      (<= constants/lines-collapse-threshold (inc line-count))))

(defn- sorted-ranges [{:keys [metadata text]} metadata-keys]
  (->> (if metadata-keys
         (select-keys metadata metadata-keys)
         metadata)
       (reduce-kv (fn [acc type ranges]
                    (reduce #(assoc %1 %2 type) acc ranges))
                  {})
       (sort-by ffirst)))

(defn emoji-only-content?
  "Determines if text is just an emoji"
  [{:keys [text response-to]}]
  (and (not response-to)
       (string? text)
       (re-matches constants/regx-emoji text)))
