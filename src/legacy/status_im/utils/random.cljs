(ns legacy.status-im.utils.random
  (:require
    ["chance" :as Chance]
    [re-frame.core :as re-frame]
    [utils.datetime :as datetime]))

(def chance (Chance.))

(defn guid
  []
  (.guid ^js chance))

(defn id
  []
  (str (datetime/timestamp) "-" (.guid ^js chance)))

(defn rand-gen
  [seed]
  (Chance. seed))

(defn seeded-rand-int
  [^js gen n]
  (.integer ^js gen (clj->js {:min 0 :max (dec n)})))

(defn seeded-rand-nth
  [gen coll]
  (nth coll (seeded-rand-int gen (count coll))))

(re-frame/reg-cofx
 :random-guid-generator
 (fn [coeffects _]
   (assoc coeffects :random-guid-generator guid)))

(re-frame/reg-cofx
 :random-id-generator
 (fn [coeffects _]
   (assoc coeffects :random-id-generator id)))
