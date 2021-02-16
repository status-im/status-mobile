(ns status-im.utils.platform
  (:require ["react-native" :as react-native :refer (Dimensions)]))

(def platform
  (.-Platform react-native))

(def os
  (when platform
    (.-OS ^js platform)))

(def version
  (when platform
    (.-Version ^js platform)))

;; iPhone X dimensions
(def x-height 812)
(def xs-height 896)

(defn iphone-x-dimensions? []
  (let [{:keys [height]} (-> Dimensions
                             (.get "window")
                             (js->clj :keywordize-keys true))]
    (or (= height x-height) (= height xs-height))))

(def android? (= os "android"))
(def ios? (= os "ios"))
(def iphone-x? (and ios? (iphone-x-dimensions?)))

(defn no-backup-directory []
  (cond
    android? "/../no_backup"
    ios?     "/"))

(defn android-version>= [v]
  (and android? (>= version v)))

(def low-device? (and android? (< version 29)))
