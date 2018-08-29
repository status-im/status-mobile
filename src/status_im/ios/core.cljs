(ns status-im.ios.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            status-im.utils.db
            status-im.ui.screens.db
            status-im.ui.screens.events
            status-im.ui.screens.subs
            status-im.data-store.core
            [status-im.ui.screens.views :as views]
            [status-im.ui.components.react :as react]
            [status-im.notifications.core :as notifications]
            [status-im.core :as core]
            [status-im.utils.instabug :as instabug]
            [status-im.utils.snoopy :as snoopy]))

(defn app-state-change-handler [state]
  (dispatch [:app-state-change state]))

(defn app-root []
  (let [keyboard-height (subscribe [:get :keyboard-height])]
    (reagent/create-class
     {:component-will-mount
      (fn []
        (.addListener react/keyboard
                      "keyboardWillShow"
                      (fn [e]
                        (let [h (.. e -endCoordinates -height)]
                          (when-not (= h @keyboard-height)
                            (dispatch [:set :keyboard-height h])
                            (dispatch [:set :keyboard-max-height h])))))
        (.addListener react/keyboard
                      "keyboardWillHide"
                      #(when-not (= 0 @keyboard-height)
                         (dispatch [:set :keyboard-height 0])))
        (.hide react/splash-screen)
        (.addEventListener react/app-state "change" app-state-change-handler))
      :component-did-mount
      (fn []
        (notifications/init))
      :component-will-unmount
      (fn []
        (.stop react/http-bridge)
        (.removeEventListener react/app-state "change" app-state-change-handler))
      :display-name "root"
      :reagent-render views/main})))

(defn init []
  (core/init app-root)
  (snoopy/subscribe!)
  (instabug/init))
