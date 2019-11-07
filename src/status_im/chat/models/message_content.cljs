(ns status-im.chat.models.message-content
  (:require [clojure.string :as string]
            [status-im.utils.platform :as platform]
            [status-im.constants :as constants]))

(def stylings [[:bold   constants/regx-bold]
               [:italic constants/regx-italic]
               [:backquote constants/regx-backquote]])

(def styling-keys (into #{} (map first) stylings))

(def ^:private actions [[:link    constants/regx-url]
                        [:tag     constants/regx-tag]
                        [:mention constants/regx-mention]])

(defn- blank-string [size]
  (.repeat " " size))

(defn- clear-ranges [ranges input]
  (reduce (fn [acc [start end]]
            (.concat (subs acc 0 start) (blank-string (- end start)) (subs acc end)))
          input ranges))

(defn- query-regex [regex content]
  (loop [input   content
         matches []
         offset  0]
    (if-let [match (.exec regex input)]
      (let [match-value    (first match)
            match-size     (count match-value)
            relative-index (.-index match)
            start-index    (+ offset relative-index)
            end-index      (+ start-index match-size)]
        (recur (subs input (+ relative-index match-size))
               (conj matches [start-index end-index])
               end-index))
      (seq matches))))

(defn- right-to-left-text? [text]
  (and (seq text)
       (re-matches constants/regx-rtl-characters (first text))))

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

(defn build-render-recipe
  "Builds render recipe from message text and metadata, can be used by render code
  by simply iterating over it and paying attention to `:kind` set for each segment of text.
  Optional in optional 2 arity version, you can pass collection of keys determining which
  metadata to include in the render recipe (all of them by default)."
  ([content]
   (build-render-recipe content nil))
  ([{:keys [text metadata] :as content} metadata-keys]
   (when metadata
     (let [[offset builder] (->> (sorted-ranges content metadata-keys)
                                 (reduce (fn [[offset builder] [[start end] kind]]
                                           (if (< start offset)
                                             [offset builder] ;; next record is nested, not allowed, discard
                                             (let [record-text (subs text start end)
                                                   record      (if (styling-keys kind)
                                                                 [(subs record-text 1
                                                                        (dec (count record-text))) kind]
                                                                 [record-text kind])]
                                               (if-let [padding (when-not (= offset start)
                                                                  [(subs text offset start) :text])]
                                                 [end (conj builder padding record)]
                                                 [end (conj builder record)]))))
                                         [0 []]))
           end-record       (when-not (= offset (count text))
                              [(subs text offset (count text)) :text])]
       (cond-> builder
         end-record (conj end-record))))))

(defn emoji-only-content?
  "Determines if text is just an emoji"
  [{:keys [text response-to]}]
  (and (not response-to)
       (string? text)
       (re-matches constants/regx-emoji text)))
