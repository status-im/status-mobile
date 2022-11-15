(ns utils
  (:require [clojure.string :as string]
            [goog.string :as gstring]
            [goog.string.format]))

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

(defn- caught-invalid-number []
  (throw (js/Error "Invalid Number")))

(defn- with-precision [precision input-number]
  (gstring/format (str "%." precision "f") input-number))

(defn- is-bad-number [number-string]
  (nil? (re-find #"^\d+(\.\d{1,9})?$" number-string)))

(defn- with-precision-division [numerator-num denominator-num precision]
  (->> (/ numerator-num denominator-num)
       (with-precision precision)))

(defn- number->formatted-number [number-string precision]
  (let [parsed-number (js/parseInt number-string 10)
        numeric-value (if (or (neg? number-string)
                              (is-bad-number (str number-string)))
                        (caught-invalid-number)
                        parsed-number)
        million       1000000
        billion       1000000000
        >=-and-<      #(and
                        (>= %1 %2)
                        (< %1 %3))
        unit          (cond
                        (>=-and-< parsed-number billion js/Number.MAX_SAFE_INTEGER) "b"
                        (>=-and-< parsed-number million billion) "m"
                        :else
                        "k")
        denominator   (cond
                        (= unit "m") million
                        (= unit "b") billion
                        :else 1000)]
    (if (js/isNaN numeric-value)
      (caught-invalid-number)
      (if (>= numeric-value 1000)
        (str (with-precision-division numeric-value denominator precision)
             unit)
        (str numeric-value)))))

(defn format-number
  "Returns the thousands in a number in kilo format

   Arguments:

   number-string (or string int) The input of value of the number

   Examples:
   [1000 0]       -> 1k
   [1001 0]       -> 1k
   [10000 0]      -> 10k
   [5000 0]       -> 5k
   [5000 0]       -> 5k
   [5000000 0]    -> 5m
   [5000 0]       -> 5k"
  [number-string precision]
  (number->formatted-number number-string precision))
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

(defn clean-text [s]
  (-> s
      (string/replace #"\n" "")
      (string/replace #"\r" "")
      (string/trim)))

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
