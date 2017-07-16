(ns status-im.utils.random
  (:require [status-im.js-dependencies :as dependencies]))

(defn timestamp []
  (.getTime (js/Date.)))

(def chance (dependencies/Chance.))

(defn id []
  (str (timestamp) "-" (.guid chance)))
