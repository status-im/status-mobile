(ns status-im.utils.platform
  (:require [status-im.android.platform :as android]
            [status-im.ios.platform :as ios]
            [status-im.desktop.platform :as desktop]
            ["react-native" :as react-native]))

(def platform
  (.-Platform react-native))

(def os
  (when platform
    (.-OS ^js platform)))

(def version
  (when platform
    (.-Version ^js platform)))

(def android? (= os "android"))
(def ios? (= os "ios"))
(def desktop? (= os "desktop"))
(def mobile? (not= os "desktop"))
(def iphone-x? (and ios? (ios/iphone-x-dimensions?)))

(def isMacOs? (when platform (.-isMacOs ^js platform)))
(def isNix? (when platform (or (.-isLinux ^js platform) (.-isUnix ^js platform))))
(def isWin? (when platform (.-isWin ^js platform)))

(def platform-specific
  (cond
    android? android/platform-specific
    ios? ios/platform-specific
    :else desktop/platform-specific))

(defn no-backup-directory []
  (cond
    android? "/../no_backup"
    ios?     "/"))

(defn android-version>= [v]
  (and android? (>= version v)))
