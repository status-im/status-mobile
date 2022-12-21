(ns quo.platform
  (:require ["react-native" :as rn]))

(def platform (.-Platform ^js rn))

(def os (when platform (.-OS platform)))

(def android? (= os "android"))
(def ios? (= os "ios"))
