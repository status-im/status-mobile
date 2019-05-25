(ns status-im.android.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            status-im.utils.db
            status-im.ui.screens.db
            status-im.ui.screens.events
            status-im.subs
            status-im.data-store.core
            [status-im.ui.screens.views :as views]
            [status-im.ui.components.react :as react]
            [status-im.native-module.core :as status]
            [status-im.notifications.core :as notifications]
            [status-im.core :as core]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.utils.snoopy :as snoopy]
            [taoensso.timbre :as log]))

(defn app-state-change-handler [state]
  (dispatch [:app-state-change state]))

(defn on-languages-change [event]
  (set! (.-locale rn-dependencies/i18n) (.-language event)))

(defn on-shake []
  (dispatch [:shake-event]))

(defn app-root [props]
  (let [keyboard-height (subscribe [:keyboard-height])]
    (reagent/create-class
     {:component-will-mount
      (fn []
        (.addListener react/keyboard
                      "keyboardDidShow"
                      (fn [e]
                        (let [h (.. e -endCoordinates -height)]
                          (dispatch [:hide-tab-bar])
                          (when-not (= h @keyboard-height)
                            (dispatch [:set :keyboard-height h])
                            (dispatch [:set :keyboard-max-height h])))))
        (.addListener react/keyboard
                      "keyboardDidHide"
                      (fn [_]
                        (dispatch [:show-tab-bar])
                        (when-not (zero? @keyboard-height)
                          (dispatch [:set :keyboard-height 0]))))
        (.hide react/splash-screen)
        (.addEventListener react/app-state "change" app-state-change-handler)
        (.addEventListener rn-dependencies/react-native-languages "change" on-languages-change)
        (.addEventListener rn-dependencies/react-native-shake
                           "ShakeEvent"
                           on-shake))
      :component-did-mount
      (fn [this]
        (dispatch [:set-initial-props (reagent/props this)]))
      :component-will-unmount
      (fn []
        (.stop react/http-bridge)
        (.removeEventListener react/app-state "change" app-state-change-handler)
        (.removeEventListener rn-dependencies/react-native-languages "change" on-languages-change)
        (.removeEventListener rn-dependencies/react-native-shake
                              "ShakeEvent"
                              on-shake))
      :display-name "root"
      :reagent-render views/main})))

(defn init []
  (status/set-soft-input-mode status/adjust-resize)
  (core/init app-root)
  (snoopy/subscribe!))
