(ns status-im.test.utils.utils
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.utils.utils :as u]))

(deftest wrap-as-call-once-test
  (let [count (atom 0)]
    (letfn [(inc-count [] (swap! count inc))]
      (let [f (u/wrap-call-once! inc-count)]
        (is (nil? (f)))
        (is (= 1 @count))
        (is (nil? (f)))
        (is (= 1 @count))))))

(deftest truncate-str-test
  (is (= (u/truncate-str "Long string" 7) "Long...")) ; threshold is less then string length
  (is (= (u/truncate-str "Long string" 11) "Long string")) ; threshold is the same as string length
  (is (= (u/truncate-str "Long string" 20) "Long string"))) ; threshold is more then string length

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
  (is (= {} (u/map-values {} inc)))
  (is (= {:a 1} (u/map-values {:a 0} inc)))
  (is (= {:a 1 :b 2} (u/map-values {:a 0 :b 1} inc))))
