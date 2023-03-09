(ns utils.number-test
  (:require [cljs.test :refer [deftest is]]
            [utils.number :as utils-number]))

(deftest parse-int
  (is (nil? (utils-number/parse-int nil)))
  (is (nil? (utils-number/parse-int "")))
  (is (= :default-value (utils-number/parse-int "" :default-value)))
  (is (= 10 (utils-number/parse-int "10")))
  (is (= 10 (utils-number/parse-int "10.99")))
  (is (= -10 (utils-number/parse-int "-10"))))
