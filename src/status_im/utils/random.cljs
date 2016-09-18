(ns status-im.utils.random
  (:require [cljsjs.chance]))

(defn timestamp []
  (.getTime (js/Date.)))

(defn id []
  (str (timestamp) "-" (.guid js/chance)))
