(ns status-im2.core
  (:require
    ;; NOTE: Do NOT sort i18n-resources because it MUST be loaded first.
    [status-im2.setup.i18n-resources :as i18n-resources]

    #_{:clj-kondo/ignore [:unsorted-required-namespaces]}
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [re-frame.interop :as interop]
    [react-native.async-storage :as async-storage]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.shake :as react-native-shake]
    [reagent.impl.batching :as batching]
    status-im.events
    status-im.subs.root
    [status-im.utils.universal-links.core :as utils.universal-links]
    [status-im2.common.log :as log]
    [status-im2.config :as config]
    [status-im2.contexts.push-notifications.events :as notifications]
    [status-im2.contexts.shell.jump-to.state :as shell.state]
    [status-im2.contexts.shell.jump-to.utils :as shell.utils]
    status-im2.events
    status-im2.navigation.core
    [status-im2.setup.dev :as dev]
    [status-im2.setup.global-error :as global-error]
    [status-im2.setup.interceptors :as interceptors]
    status-im2.subs.root
    [utils.i18n :as i18n]))

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
  (interceptors/register-global-interceptors)

  ;; Shell
  (async-storage/get-item :selected-stack-id #(shell.utils/change-selected-stack-id % nil nil))
  (async-storage/get-item :screen-height #(reset! shell.state/screen-height %))

  ;; Note - We have to enable layout animations manually at app startup, otherwise, they will be
  ;; enabled at runtime when they are used and will cause few bugs.
  ;; https://github.com/status-im/status-mobile/issues/16693. We can remove this call, once
  ;; reanimated library is upgraded to v3. Also, we can't move this call to reanimated.cljs file,
  ;; because that causes component tests to fail. (as function is not mocked in library jestUtils)
  (reanimated/enable-layout-animations true)

  (dev/setup)

  (re-frame/dispatch-sync [:app-started]))
