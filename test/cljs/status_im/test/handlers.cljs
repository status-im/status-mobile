(ns status-im.test.handlers
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.handlers :as h]))

(deftest test-set-val
  (is (= {:key :val} (h/set-el {} [nil :key :val]))))
