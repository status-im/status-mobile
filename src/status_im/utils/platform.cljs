(ns status-im.utils.platform
  (:require [status-im.android.platform :as android]
            [status-im.ios.platform :as ios]))

(def react-native (js/require "react-native"))

(def platform
  (when-let [pl (.-Platform react-native)]
    (.-OS pl)))

(def android? (= platform "android"))
(def ios? (= platform "ios"))

(def platform-specific
  (cond
    android? android/platform-specific
    ios? ios/platform-specific
    :else {}))
