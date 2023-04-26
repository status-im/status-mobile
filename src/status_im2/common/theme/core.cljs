(ns status-im2.common.theme.core
  (:require [quo.theme :as quo]
            [quo2.theme :as quo2]
            [utils.re-frame :as rf]
            [oops.core :refer [oget]]
            [react-native.core :as rn]
            [react-native.platform :as platform]))

(def device-theme (atom (rn/get-color-scheme)))

(defn change-device-theme
  [theme]
  (when-not (= theme @device-theme)
    (reset! device-theme theme)
    (rf/dispatch [:system-theme-mode-changed (keyword theme)])))

;; Appearance change listener fires false events in ios when the app is in the background
;; So, we are ignoring those events and when the device returns form the background,
;; we are manually checking the device theme, in ::app-state-change-fx
;; https://github.com/status-im/status-mobile/issues/15708
(defn add-device-theme-change-listener
  []
  (rn/appearance-add-change-listener
   #(when (or platform/android?
              (not= (oget rn/app-state "currentState") "background"))
      (change-device-theme (oget % "colorScheme")))))

(defn device-theme-dark?
  []
  (= @device-theme "dark"))

(defn set-theme
  [value]
  (quo/set-theme value)
  (quo2/set-theme value))
