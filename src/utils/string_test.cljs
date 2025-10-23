(ns utils.string-test
  (:require
    [cljs.test :refer [are deftest is]]
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
   "A"   "ab"          2
   "AB"  "a b"         2
   "ABC" "a b c d"     3
   "ABC" " a  b  c  d" 3))

(deftest remove-trailing-slash-test
  (are [expected input]
   (= expected (utils.string/remove-trailing-slash input))
   "http://example.com"      "http://example.com/"
   "http://example.com"      "http://example.com"
   "http://example.com/path" "http://example.com/path/"
   "http://example.com/path" "http://example.com/path"
   ""                        ""
   nil                       nil))

(deftest remove-http-prefix-test
  (are [expected input]
   (= expected (utils.string/remove-http-prefix input))
   "example.com"      "http://example.com"
   "example.com"      "https://example.com"
   "example.com"      "example.com"
   "example.com/path" "http://example.com/path"
   "example.com/path" "https://example.com/path"
   "example.com/path" "example.com/path"
   ""                 ""
   nil                nil))

(deftest truncate-str-test
  ; threshold is less then string length
  (is (= (utils.string/truncate "Long string" 7) "Long..."))
  ; string length (truncate middle)
  (is (= (utils.string/truncate "Long string" 7 true) "Lo...ng")) ; threshold is less then

  ; threshold is the same as string length
  (is (= (utils.string/truncate "Long string" 11) "Long string"))
  (is (= (utils.string/truncate "Long string" 20) "Long string")))
