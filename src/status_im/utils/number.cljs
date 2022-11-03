(ns status-im.utils.number
  (:require [goog.string :as gstring]
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
