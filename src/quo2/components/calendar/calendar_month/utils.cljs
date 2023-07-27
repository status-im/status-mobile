(ns quo2.components.calendar.calendar-month.utils
  (:require [utils.i18n :as i18n]))

(def ^:const months-with-year
  "Maps the corresponding string representation of a month
   By it's numeric index as in cljs-time"
  {1  "january-year"
   2  "february-year"
   3  "march-year"
   4  "april-year"
   5  "may-year"
   6  "june-year"
   7  "july-year"
   8  "august-year"
   9  "september-year"
   10 "october-year"
   11 "november-year"
   12 "december-year"})

(defn format-month-year
  [year month]
  (let [month (cond
                (or (nil? month) (zero? month)) 1
                (> month 12)                    12
                :else                           month)]
    (str (i18n/label (get months-with-year month) {:year year}))))

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
