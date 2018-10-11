(ns status-im.chat.models.message-content
  (:require [clojure.string :as string]
            [status-im.constants :as constants]))

(def ^:private actions {:link    constants/regx-url
                        :tag     constants/regx-tag
                        :mention constants/regx-mention})

(def ^:private stylings {:bold   constants/regx-bold
                         :italic constants/regx-italic})

(def ^:private styling-characters #"\*|~")

(def ^:private type->regex (merge actions stylings))

(defn- right-to-left-text? [text]
  (and (seq text)
       (re-matches constants/regx-rtl-characters (first text))))

(defn- query-regex [regex content]
  (loop [input   content
         matches []
         offset  0]
    (if-let [match (.exec regex input)]
      (let [match-value    (aget match 0)
            relative-index (.-index match)
            start-index    (+ offset relative-index)
            end-index      (+ start-index (count match-value))]
        (recur (apply str (drop end-index input))
               (conj matches [start-index end-index])
               end-index))
      (seq matches))))

(defn enrich-content
  "Enriches message content with `:metadata` and `:rtl?` information.
  Metadata map keys can by any of the `:link`, `:tag`, `:mention` actions
  or `:bold` and `:italic` stylings.
  Value for each key is sequence of tuples representing ranges in original
  `:text` content. "
  [{:keys [text] :as content}]
  (let [metadata (reduce-kv (fn [metadata type regex]
                              (if-let [matches (query-regex regex text)]
                                (assoc metadata type matches)
                                metadata))
                            {}
                            type->regex)]
    (cond-> content
      (seq metadata) (assoc :metadata metadata)
      (right-to-left-text? text) (assoc :rtl? true))))

(defn- sorted-ranges [{:keys [metadata text]}]
  (->> metadata
       (reduce-kv (fn [acc type ranges]
                    (reduce #(assoc %1 %2 type) acc ranges))
                  {})
       (sort-by (comp (juxt first second) first))
       (cons [[0 (count text)] :text])))

(defn- last-index [result]
  (or (some-> result peek :end) 0))

(defn- start [[[start]]] start)

(defn- end [[[_ end]]] end)

(defn- kind [[_ kind]] kind)

(defn- result-record [start end path]
  {:start start
   :end   end
   :kind  (into #{} (map kind) path)})

(defn build-render-recipe
  "Builds render recipe from message text and metadata, can be used by render code
  by simply iterating over it and paying attention to `:kind` set for each segment of text."
  [{:keys [text metadata] :as content}]
  (letfn [(builder [[top :as stack] [input & rest-inputs :as inputs] result]
            (if (seq input)
              (cond
                ;; input is child of the top
                (and (<= (start input) (end top))
                     (<= (end input) (end top)))
                (recur (conj stack input) rest-inputs
                       (conj result (result-record (last-index result) (start input) stack)))
                ;; input overlaps top, it's neither child, nor sibling, discard input
                (and (>= (start input) (start top))
                     (<= (start input) (end top)))
                (recur stack rest-inputs result)
                ;; the only remaining possibility, input is next sibling to top
                :else
                (recur (rest stack) inputs
                       (conj result (result-record (last-index result) (end top) stack))))
              ;; inputs consumed, unwind stack
              (loop [[top & rest-stack :as stack] stack
                     result                       result]
                (if top
                  (recur rest-stack
                         (conj result (result-record (last-index result) (end top) stack)))
                  result))))]
    (when metadata
      (let [[head & tail] (sorted-ranges content)]
        (->> (builder (list head) tail [])
             (keep (fn [{:keys [start end kind]}]
                     (let [text-content (-> (subs text start end) ;; select text chunk & remove styling chars
                                            (string/replace styling-characters ""))]
                       (when (seq text-content) ;; filter out empty text chunks
                         [text-content kind])))))))))

(defn emoji-only-content?
  "Determines if text is just an emoji"
  [{:keys [text response-to]}]
  (and (not response-to)
       (string? text)
       (re-matches constants/regx-emoji text)))
