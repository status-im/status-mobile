(ns status-im.ui.screens.profile.photo-capture.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.icons.custom-icons :as custom-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.profile.photo-capture.styles :as styles]
            [status-im.utils.image-processing :as image-processing]
            [taoensso.timbre :as log]))

(defn image-captured [data]
  (let [path       (.-uri data)
        _          (log/debug "Captured image: " path)
        on-success (fn [base64]
                     (log/debug "Captured success: " base64)
                     (re-frame/dispatch [:my-profile/update-picture base64])
                     (re-frame/dispatch [:navigate-back]))
        on-error   (fn [type error]
                     (log/debug type error))]
    (image-processing/img->base64 path on-success on-error)))

(defn profile-photo-capture []
  (let [camera-ref (reagent/atom nil)]
    [react/view styles/container
     [status-bar/status-bar]
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/image-source-title)]]
     [camera/camera {:style         {:flex 1}
                     :aspect        (:fill camera/aspects)
                     :captureQuality "480p"
                     :captureTarget (:disk camera/capture-targets)
                     :type          "front"
                     :ref           #(reset! camera-ref %)}]
     [react/view {:style {:padding 10}}
      [react/touchable-highlight {:style    {:align-self "center"}
                                  :on-press (fn []
                                              (let [camera @camera-ref]
                                                (-> (.takePictureAsync camera)
                                                    (.then image-captured)
                                                    (.catch #(log/warn "Error capturing image: " %)))))}
       [react/view
        [custom-icons/ion-icon {:name  :md-camera
                                :style {:font-size 36}}]]]]]))
