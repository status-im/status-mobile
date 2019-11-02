(ns status-im.test.utils.utils
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.utils.core :as u]
            [status-im.utils.utils :as uu]))

(deftest truncate-str-test
  (is (= (u/truncate-str "Long string" 7) "Long...")) ; threshold is less then string length
  (is (= (u/truncate-str "Long string" 7 true) "Lo...ng")) ; threshold is less then string length (truncate middle)
  (is (= (u/truncate-str "Long string" 11) "Long string")) ; threshold is the same as string length
  (is (= (u/truncate-str "Long string" 20) "Long string"))) ; threshold is more then string length

(deftest unread-messages-count-test
  (is (= (uu/unread-messages-count 2) "2"))
  (is (= (uu/unread-messages-count 12) "12"))
  (is (= (uu/unread-messages-count 400) "400"))
  (is (= (uu/unread-messages-count 1220) "1K+"))
  (is (= (uu/unread-messages-count 4353) "4K+"))
  (is (= (uu/unread-messages-count 4999) "4K+"))
  (is (= (uu/unread-messages-count 11000) "10K+")))

(deftest clean-text-test
  (is (= (u/clean-text "Hello! \n\r") "Hello!")
      (= (u/clean-text "Hello!") "Hello!")))

(deftest first-index-test
  (is (= 2 (u/first-index (partial = :test)
                          '(:a :b :test :c :test))))
  (is (= nil (u/first-index (partial = :test)
                            '(:a :b :c)))))

(deftest hash-tag?-test
  (is (u/hash-tag? "#clojure"))
  (is (not (u/hash-tag? "clojure")))
  (is (not (u/hash-tag? "clo#jure")))
  (is (not (u/hash-tag? "clojure#"))))

(deftest update-if-present-test
  (is (= {:a 1} (u/update-if-present {:a 0} :a inc)))
  (is (= {:a 2} (u/update-if-present {:a 0} :a + 2)))
  (is (= {:a 0} (u/update-if-present {:a 0} :b inc))))

(deftest map-values-test
  (is (= {} (u/map-values inc {})))
  (is (= {:a 1} (u/map-values inc {:a 0})))
  (is (= {:a 1 :b 2} (u/map-values inc {:a 0 :b 1}))))

(deftest deep-merge-test
  (is (= {} (u/deep-merge {} {})))
  (is (= {:a 1 :b 2} (u/deep-merge {:a 1} {:b 2})))
  (is (= {:a {:b 1 :c 2}} (u/deep-merge {:a {:b 1 :c 1}} {:a {:c 2}})))
  (is (= {:a {:b {:c 2}} :d 1} (u/deep-merge {:a {:b {:c 1}} :d 1} {:a {:b {:c 2}}}))))

(deftest format-decimals-test
  (is (= "1" (uu/format-decimals 1 5)))
  (is (= "1.1" (uu/format-decimals 1.1 5)))
  (is (= "1.111111" (uu/format-decimals 1.111111 7)))
  (is (= "1.1" (uu/format-decimals 1.111 1))))