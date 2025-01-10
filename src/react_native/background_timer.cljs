(ns react-native.background-timer
  (:require
    ["react-native-background-timer" :default background-timer]))

(defn set-timeout
  [cb ms]
  (.setTimeout background-timer cb ms))

(defn clear-timeout
  [id]
  (.clearTimeout background-timer id))
