(ns status-im.utils.money-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.money :as money]))

(deftest wei->ether
  (testing "Numeric input, 15 significant digits"
    (is (= (str (money/wei->ether 111122223333444000))
           "0.111122223333444")))
  (testing "String input, 18 significant digits"
    (is (= (str (money/wei->ether "111122223333441239"))
           "0.111122223333441239"))))

(deftest valid?
  (is (not (true? (money/valid? nil))))
  (is (true? (money/valid? (money/bignumber 0))))
  (is (true? (money/valid? (money/bignumber 1))))
  (is (not (true? (money/valid? (money/bignumber -1))))))

(deftest normalize
  (is (= nil (money/normalize nil)))
  (is (= "1" (money/normalize "  1 ")))
  (is (= "1.1" (money/normalize "1.1")))
  (is (= "1.1" (money/normalize "1,1"))))
