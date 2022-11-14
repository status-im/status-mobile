(ns utils
  (:require [goog.string.format]))

(defn naive-round
  "Quickly and naively round number `n` up to `decimal-places`.

  Example usage: use it to avoid re-renders caused by floating-point number
  changes in Reagent atoms. Such numbers can be rounded up to a certain number
  of `decimal-places` in order to avoid re-rendering due to tiny fractional
  changes.

  Don't use this function for arbitrary-precision arithmetic."
  [n decimal-places]
  (let [scale (Math/pow 10 decimal-places)]
    (/ (Math/round (* n scale))
       scale)))

(defn truncate-str-memo
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

(def truncate-str (memoize truncate-str-memo))

(defn first-index
  "Returns first index in coll where predicate on coll element is truthy"
  [pred coll]
  (->> coll
       (keep-indexed (fn [idx e]
                       (when (pred e)
                         idx)))
       first))

(defn index-by
  "Given a collection and a unique key function, returns a map that indexes the collection.
  Similar to group-by except that the map values are single objects (depends on key uniqueness)."
  [key coll]
  (into {} (map #(vector (key %) %) coll)))
