(ns react-native.net-info
  (:require
    ["@react-native-community/netinfo" :default net-info]))

(defn add-net-info-listener
  [callback]
  (when net-info
    (.addEventListener ^js net-info callback)))
