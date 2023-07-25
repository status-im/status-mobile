(ns quo2.components.calendar.calendar.days-grid.utils
  (:require
    [utils.number :as number-utils]
    [cljs-time.core :as t]))

(defn- day-of-week
  [date]
  (let [day (t/day-of-week date)]
    (mod day 7)))

(defn- add-days
  [date days]
  (t/plus date (t/days days)))

(defn day-grid
  [year month]
  (let [year      (number-utils/parse-int year)
        month     (number-utils/parse-int month)
        first-day (t/date-time year month 1)
        start-day (add-days first-day (- 0 (day-of-week first-day)))
        end-day   (add-days start-day 34)]
    (loop [days        []
           current-day start-day]
      (if (t/after? current-day end-day)
        days
        (recur (conj days current-day) (add-days current-day 1))))))

(defn get-day-state
  [day today year month start-date end-date]
  (cond
    (and start-date (t/equal? day start-date))          :selected
    (and end-date (t/equal? day end-date))              :selected
    (and (= (t/year day) (t/year today))
         (= (t/month day) (t/month today))
         (= (t/day day) (t/day today)))                 :today
    (and (= (t/year day) year) (= (t/month day) month)) :default
    :else                                               :disabled))

(defn update-range
  [day start-date end-date]
  (let [new-state (cond
                    (and start-date end-date)                  {:start-date day :end-date nil}
                    (and start-date (t/equal? day start-date)) {:start-date nil :end-date nil}
                    (and end-date (t/equal? day end-date))     {:start-date nil :end-date nil}
                    (nil? start-date)                          {:start-date day :end-date nil}
                    (nil? end-date)                            {:start-date start-date :end-date day}
                    :else                                      {:start-date start-date
                                                                :end-date   end-date})]
    (if (and (:start-date new-state)
             (:end-date new-state)
             (t/after? (:start-date new-state) (:end-date new-state)))
      {:start-date (:end-date new-state) :end-date (:start-date new-state)}
      new-state)))

(defn in-range?
  [day start-date end-date]
  (and start-date end-date (t/after? day start-date) (t/before? day end-date)))

(defn get-in-range-pos
  [day start-date end-date]
  (cond
    (or (nil? start-date) (nil? end-date))     nil
    (and start-date (t/equal? day start-date)) :start
    (and end-date (t/equal? day end-date))     :end
    (in-range? day start-date end-date)        :middle
    :else                                      nil))
