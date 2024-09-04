(ns utils.number
  (:require [clojure.string :as string]))

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

(defn convert-to-whole-number
  "Converts a fractional `amount` to its corresponding whole number representation
  by dividing it by 10 raised to the power of `decimals`. This is often used in financial
  calculations where amounts are stored in their smallest units (e.g., cents) and need
  to be converted to their whole number equivalents (e.g., dollars).

  Example usage:
  (convert-to-whole-number 12345 2) ; => 123.45"
  [amount decimals]
  (/ amount (Math/pow 10 decimals)))

(defn parse-int
  "Parses `n` as an integer. Defaults to zero or `default` instead of NaN."
  ([n]
   (parse-int n 0))
  ([n default]
   (let [maybe-int (js/parseInt n 10)]
     (if (integer? maybe-int)
       maybe-int
       default))))

(defn parse-float
  "Parses `n` as a float. Defaults to zero or `default` instead of NaN."
  ([n]
   (parse-float n 0))
  ([n default]
   (let [maybe-float (js/parseFloat n 10)]
     (if (js/Number.isNaN maybe-float)
       default
       maybe-float))))

(defn value-in-range
  "Returns `num` if is in the range [`lower-bound` `upper-bound`]
  if `num` exceeds a given bound, then returns the bound exceeded."
  [number lower-bound upper-bound]
  (max lower-bound (min number upper-bound)))

(defn remove-trailing-zeroes
  [num]
  (let [parts (string/split (str num) #"\.")]
    (str (first parts)
         (if-let [decimals (second parts)]
           (if (seq (string/replace decimals #"0+$" ""))
             (str "." (string/replace decimals #"0+$" ""))
             "")
           ""))))
