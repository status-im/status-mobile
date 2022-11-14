(ns status-im2.setup.dev
  (:require [react-native.platform :as platform]
            ["react-native" :refer (DevSettings LogBox)]))

(.ignoreAllLogs LogBox)

(defn setup []
  (when (and js/goog.DEBUG platform/ios? DevSettings)
    ;;on Android this method doesn't work
    (when-let [nm (.-_nativeModule DevSettings)]
      ;;there is a bug in RN, so we have to enable it first and then disable
      (.setHotLoadingEnabled ^js nm true)
      (js/setTimeout #(.setHotLoadingEnabled ^js nm false) 1000))))
