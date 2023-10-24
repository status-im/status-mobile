(ns react-native.languages
  (:require
    ["react-native-languages" :default react-native-languages]))

(defn add-change-listener
  [handler]
  (.addEventListener ^js react-native-languages "change" (fn [^js event] (handler (.-language event)))))

(defn get-lang-keyword
  []
  (keyword (.-language ^js react-native-languages)))
