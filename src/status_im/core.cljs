(ns status-im.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.error-handler :as error-handler]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.views :as views]
            [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            status-im.utils.db
            status-im.db
            status-im.subs
            status-im.events
            ["react-native-languages" :default react-native-languages]
            ["react-native-shake" :as react-native-shake]
            ["react-native-screens" :refer (enableScreens)]
            [status-im.utils.logging.core :as utils.logs]
            ["react-native" :as rn]
            [status-im.utils.snoopy :as snoopy]
            [status-im.i18n :as i18n]
            [re-frame.interop :as interop]
            [reagent.impl.batching :as batching]))

(set! interop/next-tick js/setTimeout)
(set! batching/fake-raf #(js/setTimeout % 0))

(aset js/console "disableYellowBox" true)
;; TODO we must fix all warnings, currently it's just a noise

#_(if js/goog.DEBUG
    (do
      (.unstable_enableLogBox ^js rn)
      (.ignoreWarnings (.-YellowBox ^js rn)
                       #js ["re-frame: overwriting"
                            "Warning: componentWillMount has been renamed, and is not recommended for use. See https://fb.me/react-async-component-lifecycle-hooks for details."
                            "Warning: componentWillUpdate has been renamed, and is not recommended for use. See https://fb.me/react-async-component-lifecycle-hooks for details."]))
    (aset js/console "disableYellowBox" true))

(def app-registry (.-AppRegistry rn))
(def splash-screen (-> rn .-NativeModules .-SplashScreen))

(defn on-languages-change [^js event]
  (i18n/set-language (.-language event)))

(defn on-shake []
  (re-frame/dispatch [:shake-event]))

(defn app-state-change-handler [state]
  (re-frame/dispatch [:app-state-change state]))

(defn root [props]
  (reagent/create-class
   {:component-did-mount
    (fn [this]
      (.addListener ^js react/keyboard
                    (if platform/ios?
                      "keyboardWillShow"
                      "keyboardDidShow")
                    (fn [^js e]
                      (let [h (.. e -endCoordinates -height)]
                        (re-frame/dispatch-sync [:set :keyboard-height h])
                        (re-frame/dispatch-sync [:set :keyboard-max-height h]))))
      (.addListener ^js react/keyboard
                    (if platform/ios?
                      "keyboardWillHide"
                      "keyboardWDidHide")
                    (fn [_]
                      (re-frame/dispatch-sync [:set :keyboard-height 0])))
      (.addEventListener ^js react/app-state "change" app-state-change-handler)
      (.addEventListener react-native-languages "change" on-languages-change)
      (.addEventListener react-native-shake
                         "ShakeEvent"
                         on-shake)
      (re-frame/dispatch [:set-initial-props (reagent/props this)])
      (.hide ^js splash-screen))
    :component-will-unmount
    (fn []
      (.removeEventListener ^js react/app-state "change" app-state-change-handler)
      (.removeEventListener react-native-languages "change" on-languages-change))
    :display-name "root"
    :reagent-render views/main}))

(defn init [app-root]
  (utils.logs/init-logs)
  (error-handler/register-exception-handler!)
  (enableScreens)
  (re-frame/dispatch-sync [:init/app-started])
  (.registerComponent ^js app-registry "StatusIm" #(reagent/reactify-component app-root))
  (snoopy/subscribe!))
