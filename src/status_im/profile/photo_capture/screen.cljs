(ns status-im.profile.photo-capture.screen
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [clojure.walk :refer [keywordize-keys]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.components.camera :refer [camera
                                                 aspects
                                                 capture-targets]]
            [status-im.components.styles :refer [icon-back]]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.toolbar.styles :refer [toolbar-background1]]
            [status-im.utils.image-processing :refer [img->base64]]
            [status-im.profile.photo-capture.styles :as st]
            [status-im.i18n :refer [label]]
            [reagent.core :as r]
            [taoensso.timbre :as log]))

(defn image-captured [data]
  (let [path       (.-path data)
        _ (log/debug "Captured image: " path)
        on-success (fn [base64]
                     (log/debug "Captured success: " base64)
                     (dispatch [:set-in [:profile-edit :photo-path] (str "data:image/jpeg;base64," base64)])
                     (dispatch [:navigate-back]))
        on-error   (fn [type error]
                     (log/debug type error))]
    (img->base64 path on-success on-error)))

(defn profile-photo-capture []
  (let [camera-ref (r/atom nil)]
    [view st/container
     [status-bar]
     [toolbar {:title            (label :t/image-source-title)
               :nav-action       (act/back #(dispatch [:navigate-back]))
               :background-color toolbar-background1}]
     [camera {:style         {:flex 1}
              :aspect        (:fill aspects)
              :captureQuality "480p"
              :captureTarget (:disk capture-targets)
              :type          "front"
              :ref           #(reset! camera-ref %)}]
     [view {:style {:padding          10
                    :background-color toolbar-background1}}
      [touchable-highlight {:style    {:align-self "center"}
                            :on-press (fn []
                                        (let [camera @camera-ref]
                                          (-> (.capture camera)
                                              (.then image-captured)
                                              (.catch #(log/debug "Error capturing image: " %)))))}
       [view
        [ion-icon {:name  :md-camera
                   :style {:font-size 36}}]]]]]))
