(ns utils.number-test
  (:require [cljs.test :refer [deftest is testing]]
            [utils.number :as utils-number]))

(deftest parse-int
  (testing "defaults to zero"
    (is (= 0 (utils-number/parse-int nil))))

  (testing "accepts any other default value"
    (is (= 3 (utils-number/parse-int "" 3)))
    (is (= :invalid-int (utils-number/parse-int "" :invalid-int))))

  (testing "valid numbers"
    (is (= -6 (utils-number/parse-int "-6a" 0)))
    (is (= 6 (utils-number/parse-int "6" 0)))
    (is (= 6 (utils-number/parse-int "6.99" 0)))
    (is (= -6 (utils-number/parse-int "-6" 0)))))
