(ns status-im.contexts.profile.settings.effects
  (:require [native-module.core :as native-module]
            [re-frame.core :as re-frame]
            [react-native.platform :as platform]
            [status-im.common.theme.core :as theme]
            [status-im.constants :as constants]
            [status-im.setup.hot-reload :as hot-reload]
            [utils.re-frame :as rf]))

(re-frame/reg-fx
 :profile.settings/blank-preview-flag-changed
 (fn [flag]
   (native-module/set-blank-preview-flag flag)))

(re-frame/reg-fx
 :profile.settings/webview-debug-changed
 (fn [value]
   (when platform/android?
     (native-module/toggle-webview-debug value))))

(re-frame/reg-fx
 :profile.settings/switch-theme-fx
 (fn [[theme-type view-id reload-ui?]]
   (let [theme (if (or (= theme-type constants/theme-type-dark)
                       (and (= theme-type constants/theme-type-system)
                            (theme/device-theme-dark?)))
                 :dark
                 :light)]
     (theme/set-legacy-theme theme)
     (rf/dispatch [:theme/switch theme])
     (rf/dispatch [:reload-status-nav-color view-id])
     (when reload-ui?
       (re-frame/dispatch [:dismiss-all-overlays])
       (when js/goog.DEBUG
         (hot-reload/reload))))))
