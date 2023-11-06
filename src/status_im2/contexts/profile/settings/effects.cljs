(ns status-im2.contexts.profile.settings.effects
  (:require [native-module.core :as native-module]
            [quo.foundations.colors :as colors]
            [re-frame.core :as re-frame]
            [react-native.platform :as platform]
            [status-im2.common.theme.core :as theme]
            [status-im2.constants :as constants]
            [status-im2.contexts.shell.jump-to.utils :as shell.utils]
            [status-im2.setup.hot-reload :as hot-reload]))

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
   (let [[theme status-bar-theme nav-bar-color]
         ;; Status bar theme represents status bar icons colors, so opposite to app theme
         (if (or (= theme-type constants/theme-type-dark)
                 (and (= theme-type constants/theme-type-system)
                      (theme/device-theme-dark?)))
           [:dark :light colors/neutral-100]
           [:light :dark colors/white])]
     (theme/set-theme theme)
     (re-frame/dispatch [:change-shell-status-bar-style
                         (if (shell.utils/home-stack-open?) status-bar-theme :light)])
     (when reload-ui?
       (re-frame/dispatch [:dismiss-all-overlays])
       (when js/goog.DEBUG
         (hot-reload/reload))
       (when-not (= view-id :shell-stack)
         (re-frame/dispatch [:change-shell-nav-bar-color nav-bar-color]))))))
