(ns status-im.contexts.profile.settings.effects
  (:require [native-module.core :as native-module]
            [re-frame.core :as re-frame]
            [react-native.platform :as platform]))

(re-frame/reg-fx
 :profile.settings/blank-preview-flag-changed
 (fn [flag]
   (native-module/set-blank-preview-flag flag)))

(re-frame/reg-fx
 :profile.settings/webview-debug-changed
 (fn [value]
   (when platform/android?
     (native-module/toggle-webview-debug value))))
