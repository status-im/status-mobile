(ns status-im2.core
  (:require
    [utils.i18n :as i18n]
    [status-im2.setup.i18n-resources :as i18n-resources]
    [re-frame.core :as re-frame]
    [re-frame.interop :as interop]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.shake :as react-native-shake]
    [reagent.impl.batching :as batching]
    [status-im2.contexts.shell.jump-to.utils :as shell.utils]
    [status-im2.contexts.shell.jump-to.state :as shell.state]
    [status-im2.config :as config]
    [status-im2.setup.dev :as dev]
    [status-im2.setup.global-error :as global-error]
    [status-im2.common.log :as log]
    [status-im.async-storage.core :as async-storage]
    [native-module.core :as native-module]
    [status-im.notifications.local :as notifications]
    [status-im.utils.universal-links.core :as utils.universal-links]
    status-im.events
    status-im2.events
    status-im2.navigation.core
    status-im2.subs.root))

;;;; re-frame RN setup
(set! interop/next-tick js/setTimeout)
(set! batching/fake-raf #(js/setTimeout % 0))

(def adjust-resize 16)

(defn init
  []
  (native-module/init #(re-frame/dispatch [:signals/signal-received %]))
  (when platform/android?
    (native-module/set-soft-input-mode adjust-resize))
  (log/setup config/log-level)
  (global-error/register-handler)
  (notifications/listen-notifications)
  (.addEventListener rn/app-state "change" #(re-frame/dispatch [:app-state-change %]))
  (i18n/set-language "en")
  (i18n-resources/load-language "en")
  (react-native-shake/add-shake-listener #(re-frame/dispatch [:shake-event]))
  (utils.universal-links/initialize)

  ;; Shell
  (async-storage/get-item :selected-stack-id #(shell.utils/change-selected-stack-id % nil nil))
  (async-storage/get-item :screen-height #(reset! shell.state/screen-height %))

  (dev/setup)

  (re-frame/dispatch-sync [:app-started]))
