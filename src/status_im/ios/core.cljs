(ns status-im.ios.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            status-im.ui.screens.db
            status-im.ui.screens.events
            status-im.ui.screens.subs
            status-im.data-store.core
            [status-im.ui.screens.views :as views]
            [status-im.components.react :as react]
            [status-im.components.status :as status]
            [status-im.utils.utils :as utils]))

(defn orientation->keyword [o]
  (keyword (.toLowerCase o)))

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
         (.hide react/splash-screen))
       :component-will-unmount
       (fn []
         (.stop react/http-bridge))
       :display-name "root"
       :reagent-render views/main})))

(defn init []
  (utils/register-exception-handler)
  (status/call-module status/init-jail)
  (.registerComponent react/app-registry "StatusIm" #(reagent/reactify-component app-root))
  (dispatch-sync [:initialize-app]))
