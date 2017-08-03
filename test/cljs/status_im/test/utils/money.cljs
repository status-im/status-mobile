(ns status-im.test.utils.money
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.money :as money]))

(deftest wei->ether
  (testing "Numeric input, 15 significant digits"
    (is (= (str (money/wei->ether 111122223333444000))
           "0.111122223333444")))
  (testing "String input, 18 significant digits"
    (is (= (str (money/wei->ether "111122223333441239"))
           "0.111122223333441239"))))

