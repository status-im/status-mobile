(ns utils.string-test
  (:require [cljs.test :refer [are deftest]]
            utils.string))

(deftest get-initials-test
  (are [expected input amount-initials]
   (= expected (utils.string/get-initials input amount-initials))
   ""    nil           0
   ""    nil           1
   ""    ""            0
   ""    "ab"          0
   ""    ""            1
   "A"   "ab"          1
   "A"   " ab  "       1
   "A"   "a b"         1
   "AB"  "a b"         2
   "ABC" "a b c d"     3
   "ABC" " a  b  c  d" 3))
