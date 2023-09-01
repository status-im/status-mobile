(ns quo2.components.graph.utils
  (:require [clojure.string :as string]
            [goog.string :as gstring]))

(defn find-highest-value
  [coll]
  (apply max (map :value coll)))

(defn find-lowest-value
  [coll]
  (apply min (map :value coll)))

(defn downsample-data
  [data max-array-size]
  (let [data-size (count data)]
    (if (> data-size max-array-size)
      (let [step-size (max (/ data-size max-array-size) 1)]
        (vec (take-nth step-size data)))
      data)))

(defn format-compact-number
  [number]
  (let [abbreviations           ["" "k" "M" "B" "T"]
        log-base-1000           (js/Math.log10 1000) ; Calculate the logarithm base 1000
        magnitude               (int (/ (js/Math.log10 number) log-base-1000))
        suffix                  (nth abbreviations magnitude)
        scaled-number           (/ number (js/Math.pow 1000.0 magnitude))
        formatted-scaled-number (if (zero? (rem scaled-number 1))
                                  (int scaled-number)
                                  (-> (gstring/format "%.2f" scaled-number)
                                      (string/replace #"\.?0+$" "")))]
    (if (zero? magnitude)
      (str number)
      (if (and (>= scaled-number 1) (< scaled-number 1000))
        (str formatted-scaled-number suffix)
        (str formatted-scaled-number "0" suffix)))))

(defn calculate-x-axis-labels
  [coll num-elements]
  (let [array-length (count coll)
        partitions   (partition-all (js/Math.floor (/ array-length (min array-length num-elements)))
                                    coll)]
    (->> partitions
         (map first)
         (map :date))))

(defn calculate-y-axis-labels
  [min-value step-value no-of-steps]
  (let [labels-array (for [i (range (inc no-of-steps))]
                       (let [value          (+ min-value (* step-value i))
                             compact-number (format-compact-number value)]
                         compact-number))]
    (vec labels-array)))

(defn calculate-rounded-max
  [highest-value]
  (let [min-percentage-above 1.05 ; 5% above
        rounded-divisor      4 ; Divisor for even division by 4
        target-max           (* min-percentage-above highest-value)
        rounded-up           (js/Math.ceil target-max)
        remainder            (mod rounded-up rounded-divisor)
        y-axis-max           (if (zero? remainder)
                               rounded-up
                               (+ rounded-up (- rounded-divisor remainder)))]
    y-axis-max))

(defn calculate-rounded-min
  [lowest]
  (let [order-of-magnitude (js/Math.pow 10 (js/Math.floor (js/Math.log10 lowest)))
        rounded-min        (cond
                             (and (< lowest 1) (>= lowest 0)) 0
                             (>= lowest 1)
                             (* (js/Math.floor (/ lowest
                                                  order-of-magnitude))
                                order-of-magnitude)
                             (< lowest 0)
                             (* (calculate-rounded-max (* -1 lowest))
                                -1))]
    rounded-min))

(defn format-currency-number
  [number decimal-separator]
  (let [formatted               (-> (if (= decimal-separator :comma)
                                        (js/Intl.NumberFormat
                                         "de-DE"
                                         {:style "currency" :currency "EUR" :minimumFractionDigits 2})
                                        (js/Intl.NumberFormat
                                         "en-US"
                                         {:style "currency" :currency "USD" :minimumFractionDigits 2}))
                                    (.format number))
        separator-char          (if (= decimal-separator :comma) "," ".")
        formatted-with-decimals (if (.includes formatted separator-char)
                                  formatted
                                  (str formatted separator-char "00"))]
    formatted-with-decimals))
