(ns quo2.components.calendar.calendar.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [quo2.components.calendar.calendar.utils :as utils]
            [utils.datetime :as datetime]
            [clojure.string :as string]
            [utils.number :as number-utils]))

(deftest calendar-utils-test
  (testing "generate-years"
    (let [current-year (utils/current-year)]
      (is (= (last (utils/generate-years current-year)) (- current-year 100)))
      (is (= (first (utils/generate-years current-year)) current-year))))

  (testing "current-year"
    (let [current-year (-> (datetime/now)
                           datetime/timestamp->year-month-day-date
                           (string/split #"-")
                           first
                           number-utils/parse-int)]
      (is (= (utils/current-year) current-year))))

  (testing "current-month"
    (let [current-month (-> (datetime/now)
                            datetime/timestamp->year-month-day-date
                            (string/split #"-")
                            second
                            number-utils/parse-int)]
      (is (= (utils/current-month) current-month)))))
