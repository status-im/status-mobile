(ns legacy.status-im.utils.utils-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [legacy.status-im.utils.core :as u]
    [legacy.status-im.utils.utils :as uu]))

(deftest truncate-str-test
  (is (= (u/truncate-str "Long string" 7) "Long...")) ; threshold is less then string length
  (is (= (u/truncate-str "Long string" 7 true) "Lo...ng")) ; threshold is less then string length
                                                           ; (truncate middle)
  (is (= (u/truncate-str "Long string" 11) "Long string")) ; threshold is the same as string length
  (is (= (u/truncate-str "Long string" 20) "Long string"))) ; threshold is more then string length

(deftest first-index-test
  (is (= 2
         (u/first-index (partial = :test)
                        '(:a :b :test :c :test))))
  (is (= nil
         (u/first-index (partial = :test)
                        '(:a :b :c)))))

(deftest format-decimals-test
  (is (= "1" (uu/format-decimals 1 5)))
  (is (= "1.1" (uu/format-decimals 1.1 5)))
  (is (= "1.111111" (uu/format-decimals 1.111111 7)))
  (is (= "1.1" (uu/format-decimals 1.111 1))))
