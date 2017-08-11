(ns status-im.android.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            status-im.db
            status-im.events
            status-im.subs
            status-im.data-store.core
            [status-im.views :as views]
            [status-im.components.react :as react]
            [status-im.components.status :as status]
            [status-im.utils.utils :as utils]))

(defn init-back-button-handler! []
  (let [new-listener (fn []
                       ;; todo: it might be better always return false from
                       ;; this listener and handle application's closing
                       ;; in handlers
                       (let [stack      (subscribe [:get :navigation-stack])
                             creating?  (subscribe [:get :creating-account?])
                             result-box (subscribe [:chat-ui-props :result-box])
                             webview    (subscribe [:get :webview-bridge])]
                         (cond
                           @creating? true

                           (and @webview (:can-go-back? @result-box))
                           (do (.goBack @webview) true)

                           (< 1 (count @stack))
                           (do (dispatch [:navigate-back]) true)

                           :else false)))]
    (.addEventListener react/back-android "hardwareBackPress" new-listener)))

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
       :component-will-unmount
       (fn []
         (.stop react/http-bridge)
         (.removeEventListener react/app-state "change" app-state-change-handler))
       :display-name "root"
       :reagent-render views/main})))

(defn init []
  (utils/register-exception-handler)
  (status/call-module status/init-jail)
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root))
  (status/set-soft-input-mode status/adjust-resize)
  (init-back-button-handler!)
  (dispatch-sync [:initialize-app]))