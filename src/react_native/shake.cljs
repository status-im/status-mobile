(ns react-native.shake
  (:require
    ["react-native-shake" :as react-native-shake]))

(defn add-shake-listener
  [handler]
  (.addEventListener react-native-shake "ShakeEvent" handler))
