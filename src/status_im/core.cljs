(ns status-im.core
  (:require
    ;; NOTE: Do NOT sort i18n-resources because it MUST be loaded first.
    [status-im.setup.i18n-resources :as i18n-resources]
    #_{:clj-kondo/ignore [:unsorted-required-namespaces]}
    ["@walletconnect/react-native-compat"]
    legacy.status-im.events
    legacy.status-im.subs.root
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [re-frame.interop :as interop]
    [react-native.async-storage :as async-storage]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.shake :as react-native-shake]
    [reagent.core]
    [reagent.impl.batching :as batching]
    [status-im.common.log :as logging]
    [taoensso.timbre :as log]
    [status-im.common.universal-links :as universal-links]
    [status-im.config :as config]
    [status-im.contexts.profile.push-notifications.events :as notifications]
    [status-im.feature-flags :as ff]
    [status-im.navigation.core :as navigation]
    status-im.contexts.wallet.signals
    status-im.events
    [status-im.setup.dev :as dev]
    [status-im.setup.global-error :as global-error]
    [status-im.setup.interceptors :as interceptors]
    status-im.subs.root
    [utils.i18n :as i18n]
    [status-im.setup.status-backend-client :as status-backend-client]))

;;;; re-frame RN setup
(set! interop/next-tick js/setTimeout)
(set! batching/fake-raf #(js/setTimeout % 0))
(def functional-compiler (reagent.core/create-compiler {:function-components true}))
(reagent.core/set-default-compiler! functional-compiler)

(def adjust-resize 16)

(defn is-hermes
  []
  (boolean (.-HermesInternal js/global)))

(defn init
  []
  (status-backend-client/init)
  (navigation/init)
  (native-module/init #(re-frame/dispatch [:signals/signal-received %]))
  (when platform/android?
    (native-module/set-soft-input-mode adjust-resize))
  (logging/setup config/log-level)
  (global-error/register-handler)
  (notifications/listen-notifications)
  (.addEventListener rn/app-state "change" #(re-frame/dispatch [:app-state-change %]))
  (i18n/set-language "en")
  (i18n-resources/load-language "en")
  (react-native-shake/add-shake-listener #(re-frame/dispatch [:shake-event]))
  (universal-links/initialize)
  (interceptors/register-global-interceptors)

  ;; Shell
  (async-storage/get-item :selected-stack-id #(re-frame/dispatch [:shell/change-tab %]))

  (when config/quo-preview-enabled?
    (ff/load-flags))

  (dev/setup)
  (log/info "hermesEnabled ->" (is-hermes))

  (re-frame/dispatch-sync [:app-started])

  ;; Required for wallet-connect
  ;; https://github.com/WalletConnect/walletconnect-monorepo/issues/3235#issuecomment-1645767800
  (when-not (.-BigInt js/global)
    (set! js/BigInt (js/require "big-integer"))))
