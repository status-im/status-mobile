(ns status-im.utils.core
  (:require [clojure.string :as str]
            #?(:cljs [cljs.tools.reader.edn :as edn]
               :clj [clojure.tools.reader.edn :as edn])
            [taoensso.timbre :as log]))

(defn truncate-str
  "Given string and max threshold, trims the string to threshold length with `...`
  appended to end or in the middle if length of the string exceeds max threshold,
  returns the same string if threshold is not exceeded"
  [s threshold & [middle?]]
  (if (and s (< threshold (count s)))
    (if middle?
      (let [str-len   (count s)
            max-len   (- threshold 3)
            start-len (Math/ceil (/ max-len 2))
            end-len   (Math/floor (/ max-len 2))
            start     (subs s 0 start-len)
            end       (subs s (- str-len end-len) str-len)]
        (str start "..." end))
      (str (subs s 0 (- threshold 3)) "..."))
    s))

(defn clean-text [s]
  (-> s
      (str/replace #"\n" "")
      (str/replace #"\r" "")
      (str/trim)))

(defn first-index
  "Returns first index in coll where predicate on coll element is truthy"
  [pred coll]
  (->> coll
       (keep-indexed (fn [idx e]
                       (when (pred e)
                         idx)))
       first))

(defn hash-tag? [s]
  (= \# (first s)))

(defn update-if-present
  "Like regular `clojure.core/update` but returns original map if update key is not present"
  [m k f & args]
  (if (contains? m k)
    (apply update m k f args)
    m))

(defn map-values
  "Efficiently apply function to all map values"
  [f m]
  (into {}
        (map (fn [[k v]]
               [k (f v)]))
        m))

(defn deep-merge
  "Recursively merge maps"
  [& maps]
  (if (every? map? maps)
    (apply merge-with deep-merge maps)
    (last maps)))

(defn index-by
  "Given a collection and a unique key function, returns a map that indexes the collection.
  Similar to group-by except that the map values are single objects (depends on key uniqueness)."
  [key coll]
  (into {} (map #(vector (key %) %) coll)))

#?(:cljs
   (defn safe-read-message-content [content]
     (try
       (edn/read-string content)
       (catch :default e
         (log/warn "failed to transform message with " e)))))
