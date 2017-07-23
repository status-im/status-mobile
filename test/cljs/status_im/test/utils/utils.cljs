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