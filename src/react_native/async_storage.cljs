(ns react-native.async-storage
  (:require ["@react-native-async-storage/async-storage" :default AsyncStorage]))


(defn set-item
  [key value]
  (-> (.setItem AsyncStorage (clj->js key) (str value))
      (.catch #(js/console.error (str "Error storing data in async-storage: " %)))))

(defn get-item
  [key callback]
  (-> (.getItem AsyncStorage (clj->js key))
      (.then callback)
      (.catch #(taoensso.timbre/error (str "Error retrieving data from async-storage: " %)))))
