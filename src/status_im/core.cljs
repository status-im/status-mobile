(ns status-im.core
  (:require status-im.utils.db
            status-im.events
            status-im.subs
            status-im.navigation.core
            [re-frame.core :as re-frame]
            [re-frame.interop :as interop]
            [reagent.impl.batching :as batching]
            [status-im.notifications.local :as notifications]
            [status-im.native-module.core :as status]
            [status-im.utils.error-handler :as error-handler]
            [status-im.utils.logging.core :as utils.logs]
            [status-im.utils.platform :as platform]
            [status-im.utils.snoopy :as snoopy]
            [status-im.utils.config :as config]
            [status-im.utils.universal-links.core :as utils.universal-links]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            ["react-native" :refer (DevSettings LogBox)]
            ["react-native-languages" :default react-native-languages]
            ["react-native-shake" :as react-native-shake]))

(set! interop/next-tick js/setTimeout)
(set! batching/fake-raf #(js/setTimeout % 0))
(.ignoreAllLogs LogBox)

(defn init []
  (utils.logs/init-logs config/log-level)
  (error-handler/register-exception-handler!)
  (when platform/android?
    (status/set-soft-input-mode status/adjust-resize))
  (notifications/listen-notifications)
  (.addEventListener ^js react/app-state "change" #(re-frame/dispatch [:app-state-change %]))
  (.addEventListener react-native-languages "change" (fn [^js event]
                                                       (i18n/set-language (.-language event))))
  (.addEventListener react-native-shake "ShakeEvent" #(re-frame/dispatch [:shake-event]))

  (re-frame/dispatch-sync [:init/app-started])

  (utils.universal-links/initialize)

  ;;DEV
  (snoopy/subscribe!)
  (when (and js/goog.DEBUG platform/ios? DevSettings)
    ;;on Android this method doesn't work
    (when-let [nm (.-_nativeModule DevSettings)]
      ;;there is a bug in RN, so we have to enable it first and then disable
      (.setHotLoadingEnabled ^js nm true)
      (js/setTimeout #(.setHotLoadingEnabled ^js nm false) 1000))))
