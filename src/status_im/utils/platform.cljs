(ns status-im.utils.platform
  (:require [status-im.android.platform :as android]
            [status-im.ios.platform :as ios]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(def platform
  (when-let [pl (.-Platform rn-dependencies/react-native)]
    (.-OS pl)))

(def android? (= platform "android"))
(def ios? (= platform "ios"))
(def iphone-x? (and ios? (ios/iphone-x-dimensions?)))

(def platform-specific
  (cond
    android? android/platform-specific
    ios? ios/platform-specific
    :else {}))
