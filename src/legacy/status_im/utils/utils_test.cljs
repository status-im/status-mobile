(ns legacy.status-im.utils.utils-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [legacy.status-im.utils.core :as sut]))

(deftest truncate-str-test
  (is (= (sut/truncate-str "Long string" 7) "Long...")) ; threshold is less then string length
  (is (= (sut/truncate-str "Long string" 7 true) "Lo...ng")) ; threshold is less then string length
  ; (truncate middle)
  (is (= (sut/truncate-str "Long string" 11) "Long string")) ; threshold is the same as string
                                                             ; length
  (is (= (sut/truncate-str "Long string" 20) "Long string"))) ; threshold is more then string length

