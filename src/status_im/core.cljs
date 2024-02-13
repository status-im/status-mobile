(ns status-im.core
  (:require
    ;; NOTE: Do NOT sort i18n-resources because it MUST be loaded first.
    [status-im.setup.i18n-resources :as i18n-resources]
    #_{:clj-kondo/ignore [:unsorted-required-namespaces]}
    legacy.status-im.events
    legacy.status-im.subs.root
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [re-frame.interop :as interop]
    [reagent.impl.component :as impl.component]
    [react-native.async-storage :as async-storage]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.shake :as react-native-shake]
    [reagent.impl.batching :as batching]
    [status-im.common.log :as logging]
    [taoensso.timbre :as log]
    [status-im.common.universal-links :as universal-links]
    [status-im.config :as config]
    [status-im.contexts.profile.push-notifications.events :as notifications]
    [status-im.contexts.shell.jump-to.state :as shell.state]
    [status-im.contexts.shell.jump-to.utils :as shell.utils]
    status-im.contexts.wallet.signals
    status-im.events
    status-im.navigation.core
    [status-im.setup.dev :as dev]
    [status-im.setup.global-error :as global-error]
    [status-im.setup.interceptors :as interceptors]
    status-im.subs.root
    [utils.i18n :as i18n]
    react-native.reanimated))

;;;; re-frame RN setup
(set! interop/next-tick js/setTimeout)
(set! batching/fake-raf #(js/setTimeout % 0))

(def custom-wrapper impl.component/custom-wrapper)

(defn mock-custom-wrapper [key f]
  (if (= :shouldComponentUpdate key)
    (fn shouldComponentUpdate [nextprops nextstate]
      (this-as c
        ;; Don't care about nextstate here, we use forceUpdate
        ;; when only when state has changed anyway.
        (let [old-argv (.. c -props -argv)
              new-argv (.-argv nextprops)
              noargv (or (nil? old-argv) (nil? new-argv))
              should-update? (or noargv (try (not= old-argv new-argv)
                                             (catch :default e
                                               false)))]
          (when should-update?
            (println "Component RE_RENDERED" c)
            (try (println (clojure.data/diff old-argv new-argv))
                 (catch :default e
                   nil)))
          (cond
            (nil? f) should-update?
            noargv (.call f c c (impl.component/get-argv c) (impl.component/props-argv c nextprops))
            :else  (.call f c c old-argv new-argv)))))
    (custom-wrapper key f)))

(set! impl.component/custom-wrapper mock-custom-wrapper)

(def adjust-resize 16)

(defn is-hermes
  []
  (boolean (.-HermesInternal js/global)))

(defn init
  []

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
  (async-storage/get-item :selected-stack-id #(shell.utils/change-selected-stack-id % nil nil))
  (async-storage/get-item :screen-height #(reset! shell.state/screen-height %))

  (dev/setup)
  (log/info "hermesEnabled ->" (is-hermes))

  (re-frame/dispatch-sync [:app-started]))
