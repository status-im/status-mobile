(ns quo2.components.calendar.calendar.month-picker.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [quo2.components.calendar.calendar.month-picker.utils :as utils]))

(deftest format-month-year-test
  (testing "returns correct format for given year and month"
    (is (= (utils/format-month-year 2023 1) "January 2023"))
    (is (= (utils/format-month-year 2023 12) "December 2023"))
    (is (= (utils/format-month-year 2023 0) "January 2023"))
    (is (= (utils/format-month-year 2023 13) "December 2023"))))

(deftest next-month-test
  (testing "returns the next month and year"
    (is (= (utils/next-month 2023 1) {:year "2023" :month "2"}))
    (is (= (utils/next-month 2023 12) {:year "2024" :month "1"}))))

(deftest previous-month-test
  (testing "returns the previous month and year"
    (is (= (utils/previous-month 2023 1) {:year "2022" :month "12"}))
    (is (= (utils/previous-month 2023 12) {:year "2023" :month "11"}))))
