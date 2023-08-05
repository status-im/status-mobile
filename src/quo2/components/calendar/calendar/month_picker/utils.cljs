(ns quo2.components.calendar.calendar.month-picker.utils
  (:require [utils.datetime :as datetime]))

(defn format-month-year
  [year month]
  (let [month (cond
                (or (nil? month) (zero? month)) 1
                (> month 12)                    12
                :else                           month)]
    (str (datetime/format-long-month month) " " year)))

(defn next-month
  [year month]
  (let [new-month (if (= month 12) 1 (inc month))
        new-year  (if (= month 12) (inc year) year)]
    {:year (str new-year) :month (str new-month)}))

(defn previous-month
  [year month]
  (let [new-month (if (= month 1) 12 (dec month))
        new-year  (if (= month 1) (dec year) year)]
    {:year (str new-year) :month (str new-month)}))
