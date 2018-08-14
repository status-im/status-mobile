(ns status-im.utils.platform
  (:require [status-im.android.platform :as android]
            [status-im.ios.platform :as ios]
            [status-im.desktop.platform :as desktop]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(def platform
  (.-Platform rn-dependencies/react-native))

(def os
  (when platform
    (.-OS platform)))

(def version
  (when platform
    (.-Version platform)))

(def android? (= os "android"))
(def ios? (= os "ios"))
(def desktop? (= os "desktop"))
(def mobile? (not= os "desktop"))
(def iphone-x? (and ios? (ios/iphone-x-dimensions?)))

(def platform-specific
  (cond
    android? android/platform-specific
    ios? ios/platform-specific
    :else (desktop/platform-specific (if platform (.-isMacOs platform) true))))
