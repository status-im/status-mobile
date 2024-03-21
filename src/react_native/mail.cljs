(ns react-native.mail
  (:require
    ["react-native-mail" :default react-native-mail]))

(defn mail
  [opts callback]
  (.mail react-native-mail (clj->js opts) callback))
