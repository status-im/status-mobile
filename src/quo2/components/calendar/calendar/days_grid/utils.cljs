(ns quo2.components.calendar.calendar.days-grid.utils
  (:require
    [utils.number :as number-utils]
    [cljs-time.core :as time]))

(defn- day-of-week
  [date]
  (let [day (time/day-of-week date)]
    (mod day 7)))

(defn- add-days
  [date days]
  (time/plus date (time/days days)))

(defn day-grid
  [year month]
  (let [year      (number-utils/parse-int year)
        month     (number-utils/parse-int month)
        first-day (time/date-time year month 1)
        start-day (add-days first-day (- 0 (day-of-week first-day)))
        end-day   (add-days start-day 34)]
    (loop [days        []
           current-day start-day]
      (if (time/after? current-day end-day)
        days
        (recur (conj days current-day) (add-days current-day 1))))))

(defn get-day-state
  [day today year month start-date end-date]
  (cond
    (and start-date (time/equal? day start-date))             :selected
    (and end-date (time/equal? day end-date))                 :selected
    (and (= (time/year day) (time/year today))
         (= (time/month day) (time/month today))
         (= (time/day day) (time/day today)))                 :today
    (and (= (time/year day) year) (= (time/month day) month)) :default
    :else                                                     :disabled))

(defn update-range
  [day start-date end-date]
  (let [new-state (cond
                    (and start-date end-date)                     {:start-date day :end-date nil}
                    (and start-date (time/equal? day start-date)) {:start-date nil :end-date nil}
                    (and end-date (time/equal? day end-date))     {:start-date nil :end-date nil}
                    (nil? start-date)                             {:start-date day :end-date nil}
                    (nil? end-date)                               {:start-date start-date :end-date day}
                    :else                                         {:start-date start-date
                                                                   :end-date   end-date})]
    (if (and (:start-date new-state)
             (:end-date new-state)
             (time/after? (:start-date new-state) (:end-date new-state)))
      {:start-date (:end-date new-state) :end-date (:start-date new-state)}
      new-state)))

(defn in-range?
  [day start-date end-date]
  (and start-date end-date (time/after? day start-date) (time/before? day end-date)))

(defn get-in-range-pos
  [day start-date end-date]
  (cond
    (or (nil? start-date) (nil? end-date))        nil
    (and start-date (time/equal? day start-date)) :start
    (and end-date (time/equal? day end-date))     :end
    (in-range? day start-date end-date)           :middle))
