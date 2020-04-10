(ns quo.platform
  (:require [quo.react-native :as rn]))

(def os (when rn/platform (.-OS rn/platform)))

(def android? (= os "android"))
(def ios? (= os "ios"))
