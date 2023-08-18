(ns quo2.components.calendar.calendar.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [quo2.components.calendar.calendar.utils :as utils]
            [utils.datetime :as datetime]
            [clojure.string :as string]
            [utils.number :as utils.number]))

(deftest generate-years-test
  (testing "returns correct years range"
    (let [current-year (utils/current-year)]
      (is (= (last (utils/generate-years current-year)) (- current-year 100)))
      (is (= (first (utils/generate-years current-year)) current-year)))))

(deftest current-year-test
  (testing "returns the current year"
    (let [current-year (-> (datetime/now)
                           datetime/timestamp->year-month-day-date
                           (string/split #"-")
                           first
                           utils.number/parse-int)]
      (is (= (utils/current-year) current-year)))))

(deftest current-month-test
  (testing "returns the current month"
    (let [current-month (-> (datetime/now)
                            datetime/timestamp->year-month-day-date
                            (string/split #"-")
                            second
                            utils.number/parse-int)]
      (is (= (utils/current-month) current-month)))))
