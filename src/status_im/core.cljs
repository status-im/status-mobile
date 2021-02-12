(ns status-im.core
  (:require status-im.utils.db
            status-im.events
            status-im.subs
            [status-im.ui.screens.views :as views]
            [re-frame.core :as re-frame]
            [re-frame.interop :as interop]
            [reagent.core :as reagent]
            [reagent.impl.batching :as batching]
            [status-im.notifications.local :as notifications]
            [status-im.native-module.core :as status]
            [status-im.utils.error-handler :as error-handler]
            [status-im.utils.logging.core :as utils.logs]
            [status-im.utils.platform :as platform]
            [status-im.utils.snoopy :as snoopy]
            [status-im.utils.config :as config]
            ["react-native-screens" :refer (enableScreens)]
            ["react-native" :as rn :refer (DevSettings LogBox)]))

(set! interop/next-tick js/setTimeout)
(set! batching/fake-raf #(js/setTimeout % 0))
(.ignoreAllLogs LogBox)

(defn init []
  (utils.logs/init-logs config/log-level)
  (error-handler/register-exception-handler!)
  (enableScreens)
  (re-frame/dispatch-sync [:init/app-started])
  (when platform/android?
    (status/set-soft-input-mode status/adjust-resize))
  (.registerComponent ^js (.-AppRegistry rn) "StatusIm" #(reagent/reactify-component views/root))
  (notifications/listen-notifications)
  (when platform/android?
    (.registerHeadlessTask ^js (.-AppRegistry rn) "LocalNotifications" notifications/handle))
  (snoopy/subscribe!)
  (when (and js/goog.DEBUG platform/ios? DevSettings)
    ;;on Android this method doesn't work
    (when-let [nm (.-_nativeModule DevSettings)]
      ;;there is a bug in RN, so we have to enable it first and then disable
      (.setHotLoadingEnabled ^js nm true)
      (js/setTimeout #(.setHotLoadingEnabled ^js nm false) 1000))))
