(ns quo2.components.graph.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [quo2.components.graph.utils :as utils]))

(deftest find-highest-value
  (testing "Find highest value with a single map"
    (let [data [{:value 5}]]
      (is (= (utils/find-highest-value data) 5))))

  (testing "Find highest value with multiple maps"
    (let [data [{:value 5} {:value 10} {:value 3} {:value 7}]]
      (is (= (utils/find-highest-value data) 10))))

  (testing "Find highest value with negative values"
    (let [data [{:value -2} {:value -10} {:value -3} {:value -7}]]
      (is (= (utils/find-highest-value data) -2))))

  (testing "Find highest value with decimal values"
    (let [data [{:value 3.5} {:value 7.2} {:value 2.9}]]
      (is (= (utils/find-highest-value data) 7.2))))

  (testing "Find highest value with a large data set"
    (let [data (vec (for [num (range 1000)] {:value num}))]
      (is (= (utils/find-highest-value data) 999)))))

(deftest find-lowest-value
  (testing "Find lowest value with a single map"
    (let [data [{:value 5}]]
      (is (= (utils/find-lowest-value data) 5))))

  (testing "Find lowest value with multiple maps"
    (let [data [{:value 5} {:value 10} {:value 3} {:value 7}]]
      (is (= (utils/find-lowest-value data) 3))))

  (testing "Find lowest value with negative values"
    (let [data [{:value -2} {:value -10} {:value -3} {:value -7}]]
      (is (= (utils/find-lowest-value data) -10))))

  (testing "Find lowest value with decimal values"
    (let [data [{:value 3.5} {:value 7.2} {:value 2.9}]]
      (is (= (utils/find-lowest-value data) 2.9))))

  (testing "Find lowest value with a large data set"
    (let [data (vec (for [num (range 1000)] {:value num}))]
      (is (= (utils/find-lowest-value data) 0)))))

(deftest downsample-data
  (testing "Downsampling is applied correctly when needed"
    (let [input-data      [1 2 3 4 5 6 7 8 9 10]
          max-array-size  5
          expected-output [1 3 5 7 9]]
      (is (= (utils/downsample-data input-data max-array-size) expected-output))))

  (testing "Downsampling is not applied when not needed"
    (let [input-data      [1 2 3 4 5 6 7 8 9 10]
          max-array-size  10
          expected-output [1 2 3 4 5 6 7 8 9 10]]
      (is (= (utils/downsample-data input-data max-array-size) expected-output))))

  (testing "Downsampling works with empty input data"
    (let [input-data      []
          max-array-size  5
          expected-output []]
      (is (= (utils/downsample-data input-data max-array-size) expected-output))))

  (testing "Downsampling works with max-array-size of 1 (edge case)"
    (let [input-data      [1 2 3 4 5]
          max-array-size  1
          expected-output [1]]
      (is (= (utils/downsample-data input-data max-array-size) expected-output))))

  (testing "Downsampling works with large input data and max-array-size (randomized test)"
    (let [large-data      (range 1000)
          max-array-size  500
          expected-output (range 0 1000 2)] ; expected-output contains every 2nd element from 0 to 1000
      (is (= (utils/downsample-data large-data max-array-size) expected-output)))))

(deftest format-compact-number
  (testing "Format a whole number less than 1000"
    (is (= (utils/format-compact-number 567) "567")))

  (testing "Format a whole number exactly 1000"
    (is (= (utils/format-compact-number 1000) "1k")))

  (testing "Format a whole number greater than 1000"
    (is (= (utils/format-compact-number 2500) "2.5k")))

  (testing "Format a decimal number less than 1000"
    (is (= (utils/format-compact-number 123.45) "123.45")))

  (testing "Format a decimal number exactly 1000"
    (is (= (utils/format-compact-number 1000) "1k")))

  (testing "Format a decimal number greater than 1000"
    (is (= (utils/format-compact-number 7890.123) "7.89k")))

  (testing "Format a number in millions"
    (is (= (utils/format-compact-number 123456789) "123.46M")))

  (testing "Format a number in billions"
    (is (= (utils/format-compact-number 1234567890) "1.23B")))

  (testing "Format a number in trillions"
    (is (= (utils/format-compact-number 1234567890000) "1.23T"))))

(deftest calculate-x-axis-labels
  (testing "Calculate x-axis labels with a small array and fewer elements"
    (let [data [{:date "2023-01-01"} {:date "2023-01-02"} {:date "2023-01-03"} {:date "2023-01-04"}]]
      (is (= (utils/calculate-x-axis-labels data 2) '("2023-01-01" "2023-01-03")))))

  (testing "Calculate x-axis labels with a larger array and more elements"
    (let [data (vec (for [i (range 10)] {:date (str "2023-01-0" (inc i))}))]
      (is (= (utils/calculate-x-axis-labels data 5)
             '("2023-01-01" "2023-01-03" "2023-01-05" "2023-01-07" "2023-01-09")))))

  (testing "Calculate x-axis labels with a very small array"
    (let [data [{:date "2023-01-01"}]]
      (is (= (utils/calculate-x-axis-labels data 3) '("2023-01-01")))))

  (testing "Calculate x-axis labels with a larger array and a single element"
    (let [data (vec (for [i (range 10)] {:date (str "2023-01-0" (inc i))}))]
      (is (= (utils/calculate-x-axis-labels data 1) '("2023-01-01"))))))

(deftest calculate-y-axis-labels
  (testing "Calculate y-axis labels with positive values"
    (is (= (utils/calculate-y-axis-labels 0 10 5) ["0" "10" "20" "30" "40" "50"])))

  (testing "Calculate y-axis labels with negative values"
    (is (= (utils/calculate-y-axis-labels -20 5 4) ["-20" "-15" "-10" "-5" "0"])))

  (testing "Calculate y-axis labels with decimal step value"
    (is (= (utils/calculate-y-axis-labels 2.5 0.5 4) ["2.5" "3" "3.5" "4" "4.5"])))

  (testing "Calculate y-axis labels with a single step"
    (is (= (utils/calculate-y-axis-labels 5 1 1) ["5" "6"])))

  (testing "Calculate y-axis labels with large step value and number of steps"
    (is (= (utils/calculate-y-axis-labels 100 1000 3) ["100" "1.1k" "2.1k" "3.1k"]))))

(deftest calculate-rounded-max
  (testing "Calculate rounded max with a whole number"
    (is (= (utils/calculate-rounded-max 100) 108)))

  (testing "Calculate rounded max with a decimal number"
    (is (= (utils/calculate-rounded-max 50.5) 56)))

  (testing "Calculate rounded max with a number already divisible by divisor"
    (is (= (utils/calculate-rounded-max 108) 116)))

  (testing "Calculate rounded max with a number close to the next divisor"
    (is (= (utils/calculate-rounded-max 250) 264)))

  (testing "Calculate rounded max with a large number"
    (is (= (utils/calculate-rounded-max 1000) 1052))))

(deftest calculate-rounded-min
  (testing "Calculate rounded min with a whole number"
    (is (= (utils/calculate-rounded-min 157) 100)))

  (testing "Calculate rounded min with a decimal number"
    (is (= (utils/calculate-rounded-min 49.8) 40)))

  (testing "Calculate rounded min with a small number"
    (is (= (utils/calculate-rounded-min 0.007) 0)))

  (testing "Calculate rounded min with a number already rounded"
    (is (= (utils/calculate-rounded-min 500) 500)))

  (testing "Calculate rounded min with a negative number"
    (is (= (utils/calculate-rounded-min -63) -68))))

(deftest format-currency-number-test
  (testing "Format currency number with comma decimal separator"
    (is (= (utils/format-currency-number 12345 :comma) "12.345,00"))
    (is (= (utils/format-currency-number 12345.67 :comma) "12.345,67")))

  (testing "Format currency number with dot decimal separator"
    (is (= (utils/format-currency-number 12345 :dot) "12,345.00"))
    (is (= (utils/format-currency-number 12345.67 :dot) "12,345.67"))))
