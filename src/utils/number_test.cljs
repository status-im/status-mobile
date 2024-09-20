(ns utils.number-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [utils.number]))

(deftest convert-to-whole-number-test
  (testing "correctly converts fractional amounts to whole numbers"
    (is (= 123.45 (utils.number/convert-to-whole-number 12345 2)))
    (is (= 1.2345 (utils.number/convert-to-whole-number 12345 4)))
    (is (= 12345.0 (utils.number/convert-to-whole-number 1234500 2)))
    (is (= 0.123 (utils.number/convert-to-whole-number 123 3)))
    (is (= 1000.0 (utils.number/convert-to-whole-number 1000000 3))))

  (testing "handles zero decimals"
    (is (= 12345 (utils.number/convert-to-whole-number 12345 0))))

  (testing "handles negative amounts"
    (is (= -123.45 (utils.number/convert-to-whole-number -12345 2)))
    (is (= -1.2345 (utils.number/convert-to-whole-number -12345 4)))
    (is (= -0.123 (utils.number/convert-to-whole-number -123 3))))

  (testing "handles zero amount"
    (is (= 0 (utils.number/convert-to-whole-number 0 2)))
    (is (= 0 (utils.number/convert-to-whole-number 0 0)))))

(deftest parse-int-test
  (testing "defaults to zero"
    (is (= 0 (utils.number/parse-int nil))))

  (testing "accepts any other default value"
    (is (= 3 (utils.number/parse-int "" 3)))
    (is (= :invalid-int (utils.number/parse-int "" :invalid-int))))

  (testing "valid numbers"
    (is (= -6 (utils.number/parse-int "-6a" 0)))
    (is (= 6 (utils.number/parse-int "6" 0)))
    (is (= 6 (utils.number/parse-int "6.99" 0)))
    (is (= -6 (utils.number/parse-int "-6" 0)))))

(deftest parse-float-test
  (testing "defaults to zero"
    (is (= 0 (utils.number/parse-float nil))))

  (testing "accepts any other default value"
    (is (= 3 (utils.number/parse-float "" 3)))
    (is (= :invalid-float (utils.number/parse-float "" :invalid-float))))

  (testing "valid numbers"
    (is (= -6 (utils.number/parse-float "-6a")))
    (is (= 6 (utils.number/parse-float "6")))
    (is (= 6.99 (utils.number/parse-float "6.99" 0)))
    (is (= -6.9 (utils.number/parse-float "-6.9" 0)))))
