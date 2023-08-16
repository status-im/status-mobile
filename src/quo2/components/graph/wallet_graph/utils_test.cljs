(ns quo2.components.graph.wallet-graph.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [quo2.components.graph.wallet-graph.utils :as utils]))

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
          expected-output (range 0 1000 2)] ;; expected-output contains every 2nd element from 0 to 1000
      (is (= (utils/downsample-data large-data max-array-size) expected-output)))))
