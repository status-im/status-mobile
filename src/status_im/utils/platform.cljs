(ns status-im.utils.platform
  (:require [status-im.ios.platform :as ios]
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
(def iphone-x? (and ios? (ios/iphone-x-dimensions?)))

(defn no-backup-directory []
  (cond
    android? "/../no_backup"
    ios?     "/"))

(defn android-version>= [v]
  (and android? (>= version v)))
