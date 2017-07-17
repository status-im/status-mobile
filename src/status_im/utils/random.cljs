(ns status-im.utils.random)

(defn timestamp []
  (.getTime (js/Date.)))
 
(def Chance (js/require "chance"))

(def chance (Chance.))

(defn id []
  (str (timestamp) "-" (.guid chance)))
