(ns status-im.gateway.market.core-test
  (:require [clojure.test :refer [are deftest]]
            [status-im.gateway.market.core :refer [page-size-to-ensure-tokens-updating]]))

(deftest page-size-to-ensure-tokens-updating-test
  (are [input expected]
   (= expected (page-size-to-ensure-tokens-updating input))
   {:first-visible-index 14 :visible-count 11} {:page-size 13 :page-index 2}
   {:first-visible-index 10 :visible-count 6}  {:page-size 8 :page-index 2}
   {:first-visible-index 0 :visible-count 7}   {:page-size 7 :page-index 1}
   {:first-visible-index 15 :visible-count 0}  nil
   {:first-visible-index 0 :visible-count 1}   {:page-size 1 :page-index 1}
   {:first-visible-index 9 :visible-count 1}   {:page-size 1 :page-index 10}
   {:first-visible-index 5 :visible-count 2}   {:page-size 4 :page-index 2}
   {:first-visible-index 4 :visible-count 5}   {:page-size 9 :page-index 1}))
