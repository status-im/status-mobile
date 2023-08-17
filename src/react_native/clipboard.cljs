(ns react-native.clipboard
  (:require ["@react-native-community/clipboard" :default Clipboard]))

(defn set-string
  [text]
  (.setString ^js Clipboard text))

(defn get-string
  [callback]
  (.then (.getString ^js Clipboard) #(callback %)))
