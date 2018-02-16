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
            [status-im.utils.error-handler :as error-handler]
            [status-im.utils.utils :as utils]
            [status-im.utils.config :as config]
            [status-im.utils.notifications :as notifications]
            [status-im.core :as core]
            [status-im.utils.snoopy :as snoopy]
            [taoensso.timbre :as log]))

(defn init-back-button-handler! []
  (let [new-listener (fn []
                       ;; todo: it might be better always return false from
                       ;; this listener and handle application's closing
                       ;; in handlers
                       (let [stack      (subscribe [:get :navigation-stack])
                             creating?  (subscribe [:get :accounts/creating-account?])
                             result-box (subscribe [:get-current-chat-ui-prop :result-box])
                             webview    (subscribe [:get :webview-bridge])]
                         (cond
                           @creating? true

                           (and @webview (:can-go-back? @result-box))
                           (do (.goBack @webview) true)

                           (< 1 (count @stack))
                           (do (dispatch [:navigate-back]) true)

                           :else false)))]
    (.addEventListener react/back-handler "hardwareBackPress" new-listener)))

(defn orientation->keyword [o]
  (keyword (.toLowerCase o)))

(defn app-state-change-handler [state]
  (dispatch [:app-state-change state]))

(defn app-root []
  (let [keyboard-height (subscribe [:get :keyboard-height])]
    (reagent/create-class
      {:component-will-mount
       (fn []
         (let [o (orientation->keyword (.getInitialOrientation react/orientation))]
           (dispatch [:set :orientation o]))
         (.addOrientationListener
          react/orientation
          #(dispatch [:set :orientation (orientation->keyword %)]))
         (.lockToPortrait react/orientation)
         (.addListener react/keyboard
                       "keyboardDidShow"
                       (fn [e]
                         (let [h (.. e -endCoordinates -height)]
                           (when-not (= h @keyboard-height)
                             (dispatch [:set :keyboard-height h])
                             (dispatch [:set :keyboard-max-height h])))))
         (.addListener react/keyboard
                       "keyboardDidHide"
                       #(when-not (= 0 @keyboard-height)
                          (dispatch [:set :keyboard-height 0])))
         (.hide react/splash-screen)
         (.addEventListener react/app-state "change" app-state-change-handler))
       :component-did-mount
       (fn []
         (notifications/on-refresh-fcm-token)
         ;; TODO(oskarth): Background click_action handler
         (notifications/on-notification))
       :component-will-unmount
       (fn []
         (.stop react/http-bridge)
         (.removeEventListener react/app-state "change" app-state-change-handler))
       :display-name "root"
       :reagent-render views/main})))


(defn sleep [msec]
  (let [deadline (+ msec (.getTime (js/Date.)))]
    (while (> deadline (.getTime (js/Date.))))))

(defn init []
  (log/debug "[INIT] android.core/init")
  (log/debug "[INIT] status/init-jail")
  (status/init-jail)
  (sleep 2000)
  (log/debug "[INIT] status/set-soft-input-mode")
  (status/set-soft-input-mode status/adjust-resize)
  (log/debug "[INIT] init-back-button-handler!")
  (init-back-button-handler!)
  (log/debug "[INIT] core/init")
  (core/init app-root)
  (log/debug "[INIT] snoopy/subscribe!")
  (snoopy/subscribe!))
