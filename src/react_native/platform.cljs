(ns react-native.platform
  (:require
    ["react-native" :as react-native]))

(def platform (.-Platform ^js react-native))

(def os (when platform (.-OS ^js platform)))

(def version (when platform (.-Version ^js platform)))

(def android? (= os "android"))
(def ios? (= os "ios"))

(def low-device? (and android? (< version 29)))
