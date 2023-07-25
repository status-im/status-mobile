(ns quo2.components.calendar.calendar.utils
  (:require [utils.datetime :as dt]
            [utils.number :as number-utils]
            [clojure.string :as string]))

(defn generate-years
  [current-year]
  (let [current-year current-year]
    (reverse (vec (range (- current-year 100) (+ current-year 1))))))

(defn current-year
  []
  (-> (dt/now)
      dt/timestamp->year-month-day-date
      (string/split #"-")
      first
      number-utils/parse-int))

(defn current-month
  []
  (-> (dt/now)
      dt/timestamp->year-month-day-date
      (string/split #"-")
      second
      number-utils/parse-int))
