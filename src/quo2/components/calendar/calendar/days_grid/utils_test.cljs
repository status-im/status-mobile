(ns quo2.components.calendar.calendar.days-grid.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [quo2.components.calendar.calendar.days-grid.utils :as utils]
            [cljs-time.core :as time]))

(deftest day-grid-test
  (let [day-grid-result (utils/day-grid "2023" "7")]
    (testing "it returns correct days grid"
      (is (= 35 (count day-grid-result)))
      (is (time/equal? (time/date-time 2023 6 25) (first day-grid-result)))
      (is (time/equal? (time/date-time 2023 7 29) (last day-grid-result))))))

(deftest get-day-state-test
  (let [today      (time/date-time 2023 7 27)
        year       2023
        month      7
        start-date (time/date-time 2023 7 20)
        end-date   (time/date-time 2023 7 30)]
    (testing "it returns :today when day equals today"
      (is (= :today (utils/get-day-state today today year month start-date end-date))))
    (testing "it returns :selected when day equals start-date and not today"
      (is
       (= :selected (utils/get-day-state start-date today year month start-date end-date))))
    (testing "it returns :selected when day equals end-date and not today"
      (is
       (= :selected (utils/get-day-state end-date today year month start-date end-date))))))

(deftest update-range-test
  (let [start-date (time/date-time 2023 7 20)
        end-date   (time/date-time 2023 7 30)
        day        (time/date-time 2023 7 27)]
    (testing "it returns updated range"
      (is
       (= {:start-date day :end-date nil} (utils/update-range day start-date end-date))))))

(deftest in-range-test
  (let [start-date (time/date-time 2023 7 20)
        end-date   (time/date-time 2023 7 30)
        day        (time/date-time 2023 7 27)]
    (testing "it returns true when day is within range"
      (is (utils/in-range? day start-date end-date))
      (is (not (utils/in-range? (time/date-time 2023 7 19) start-date end-date))))))

(deftest get-in-range-pos-test
  (let [start-date (time/date-time 2023 7 20)
        end-date   (time/date-time 2023 7 30)
        day        (time/date-time 2023 7 27)]
    (testing "it returns correct position within range"
      (is (= :start (utils/get-in-range-pos start-date start-date end-date)))
      (is (= :end (utils/get-in-range-pos end-date start-date end-date)))
      (is (= :middle (utils/get-in-range-pos day start-date end-date))))))
