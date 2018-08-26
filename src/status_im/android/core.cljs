(ns status-im.android.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            status-im.utils.db
            status-im.ui.screens.db
            status-im.ui.screens.events
            status-im.ui.screens.subs
            status-im.data-store.core
            [status-im.ui.screens.views :as views]
            [status-im.ui.components.react :as react]
            [status-im.native-module.core :as status]
            [status-im.notifications.core :as notifications]
            [status-im.core :as core]
            [status-im.utils.snoopy :as snoopy]))

(defn init-back-button-handler! []
  (let [new-listener (fn []
                       ;; todo: it might be better always return false from
                       ;; this listener and handle application's closing
                       ;; in handlers
                       (let [stack      (subscribe [:get :navigation-stack])
                             result-box (subscribe [:get-current-chat-ui-prop :result-box])
                             webview    (subscribe [:get :webview-bridge])
                             view-id    (subscribe [:get :view-id])
                             chat-id    (subscribe [:get-current-chat-id])]
                         (cond

                           (and @webview (:can-go-back? @result-box))
                           (do (.goBack @webview) true)

                           (< 1 (count @stack))
                           (do (dispatch [:navigate-back]) true)

                           :else false)))]
    (.addEventListener react/back-handler "hardwareBackPress" new-listener)))

(defn app-state-change-handler [state]
  (dispatch [:app-state-change state]))

(defn app-root []
  (let [keyboard-height (subscribe [:get :keyboard-height])]
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
                        (when (zero? @keyboard-height)
                          (dispatch [:set :keyboard-height 0]))))
        (.hide react/splash-screen)
        (.addEventListener react/app-state "change" app-state-change-handler))
      :component-did-mount
      (fn []
         ;; TODO(oskarth): Background click_action handler
        (notifications/init))
      :component-will-unmount
      (fn []
        (.stop react/http-bridge)
        (.removeEventListener react/app-state "change" app-state-change-handler))
      :display-name "root"
      :reagent-render views/main})))

(defn init []
  (status/set-soft-input-mode status/adjust-resize)
  (init-back-button-handler!)
  (core/init app-root)
  (snoopy/subscribe!))
