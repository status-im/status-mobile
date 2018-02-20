(ns status-im.utils.random
  (:require [status-im.js-dependencies :as dependencies]
            [status-im.utils.datetime :as datetime]))

(def chance (dependencies/Chance.))

(defn id []
  (str (datetime/timestamp) "-" (.guid chance)))

(defn rand-gen
  [seed]
  (dependencies/Chance. seed))

(defn seeded-rand-int
  [gen n] (.integer gen #js {:min 0 :max (dec n)}))

(defn seeded-rand-nth
  [gen coll]
  (nth coll (seeded-rand-int gen (count coll))))
