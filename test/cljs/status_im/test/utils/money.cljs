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

(deftest valid?
  (is (false? (money/valid? nil)))
  (is (false? (money/valid? "a")))
  (is (false? (money/valid? "-1")))
  (is (false? (money/valid? "1a")))
  (is (true? (money/valid? "1")))
  (is (true? (money/valid? "1.1")))
  (is (true? (money/valid? "1,1"))))

(deftest normalize
  (is (= nil (money/normalize nil)))
  (is (= "1" (money/normalize "  1 ")))
  (is (= "1.1" (money/normalize "1.1")))
  (is (= "1.1" (money/normalize "1,1"))))

(deftest str->float
  (is (= nil (money/str->float nil)))
  (is (= 1 (money/str->float "  1 ")))
  (is (= 1.1 (money/str->float "1.1")))
  (is (= 1.1 (money/str->float "1,1"))))