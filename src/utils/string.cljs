(ns utils.string
  (:require [clojure.string :as string]))

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

(defn has-lower-case?
  [s]
  (some? (re-find #"[a-z]" s)))

(defn has-upper-case?
  [s]
  (some? (re-find #"[A-Z]" s)))

(defn has-numbers?
  [s]
  (some? (re-find #"\d" s)))

(defn has-symbols?
  [s]
  (some? (re-find #"[^a-zA-Z0-9\s]" s)))

(defn at-least-n-chars?
  [s n]
  (>= (count s) n))

(defn safe-trim
  [s]
  (when (string? s)
    (string/trim s)))

(defn safe-replace
  [s m r]
  (when (string? s)
    (string/replace s m r)))

(defn get-initials
  "Returns `n` number of initial letters from `s`, all uppercased."
  [s n]
  (let [words (-> s str string/trim (string/split #"\s+"))]
    (->> words
         (take n)
         (map (comp string/upper-case str first))
         string/join)))
