(ns quo2.components.calendar.calendar.utils
  (:require
    [clojure.string :as string]
    [utils.datetime :as datetime]
    [utils.number :as utils.number]))

(defn generate-years
  [current-year]
  (let [current-year current-year]
    (reverse (vec (range (- current-year 100) (inc current-year))))))

(defn current-year
  []
  (-> (datetime/now)
      datetime/timestamp->year-month-day-date
      (string/split #"-")
      first
      utils.number/parse-int))

(defn current-month
  []
  (-> (datetime/now)
      datetime/timestamp->year-month-day-date
      (string/split #"-")
      second
      utils.number/parse-int))
