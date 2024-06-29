(ns utils.map
  (:require [utils.money :as money]))

(defn compare-values
  "Compares two values, using special handling for BigNumbers and regular equality for others."
  [v1 v2]
  (cond
    (and (money/bignumber? v1) (money/bignumber? v2)) (money/equal-to v1 v2)
    :else                                             (= v1 v2)))

(defn deep-compare
  "Recursively compare two maps, specially handling BigNumber values within the maps."
  [map1 map2]
  (and
   (= (set (keys map1)) (set (keys map2)))
   (every? (fn [k] (compare-values (get map1 k) (get map2 k)))
           (keys map1))))
