(ns status-im.components.network-info
  (:require [taoensso.timbre :as log]))

(def network-info-class (js/require "react-native-network-info"))

(defn get-ip [callback]
  (.getIPAddress network-info-class
                 callback))