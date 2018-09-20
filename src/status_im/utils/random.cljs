(ns status-im.utils.random
  (:require [re-frame.core :as re-frame]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.datetime :as datetime]))

(def chance (dependencies/Chance.))

(defn guid []
  (.guid chance))

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

(re-frame/reg-cofx
 :random-guid-generator
 (fn [coeffects _]
   (assoc coeffects :random-guid-generator guid)))

(re-frame/reg-cofx
 :random-id
 (fn [coeffects _]
   (assoc coeffects :random-id (id))))

(re-frame/reg-cofx
 :random-id-seq
 (fn [coeffects _]
   (assoc coeffects :random-id-seq (repeatedly id))))
