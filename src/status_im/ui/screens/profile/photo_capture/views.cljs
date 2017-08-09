(ns status-im.ui.screens.profile.photo-capture.views
  (:require [re-frame.core :refer [dispatch]]
            [reagent.core :as r]
            [status-im.components.camera :refer [aspects camera capture-targets]]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.components.react :as react]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.actions :as actions]
            [status-im.components.toolbar.styles :refer [toolbar-background1]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.profile.photo-capture.styles :as styles]
            [status-im.utils.image-processing :refer [img->base64]]
            [taoensso.timbre :as log]))

(defn image-captured [data]
  (let [path       (.-path data)
        _          (log/debug "Captured image: " path)
        on-success (fn [base64]
                     (log/debug "Captured success: " base64)
                     (dispatch [:set-in [:profile-edit :photo-path] (str "data:image/jpeg;base64," base64)])
                     (dispatch [:navigate-back]))
        on-error   (fn [type error]
                     (log/debug type error))]
    (img->base64 path on-success on-error)))

(defn profile-photo-capture []
  (let [camera-ref (r/atom nil)]
    [react/view styles/container
     [status-bar]
     [toolbar {:title            (label :t/image-source-title)
               :nav-action       (actions/back #(dispatch [:navigate-back]))
               :background-color toolbar-background1}]
     [camera {:style         {:flex 1}
              :aspect        (:fill aspects)
              :captureQuality "480p"
              :captureTarget (:disk capture-targets)
              :type          "front"
              :ref           #(reset! camera-ref %)}]
     [react/view {:style {:padding          10
                          :background-color toolbar-background1}}
      [react/touchable-highlight {:style    {:align-self "center"}
                                  :on-press (fn []
                                              (let [camera @camera-ref]
                                                (-> (.capture camera)
                                                    (.then image-captured)
                                                    (.catch #(log/debug "Error capturing image: " %)))))}
       [react/view
        [ion-icon {:name  :md-camera
                   :style {:font-size 36}}]]]]]))
