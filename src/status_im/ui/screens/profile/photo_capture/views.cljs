(ns status-im.ui.screens.profile.photo-capture.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.profile.photo-capture.styles :as styles]
            [status-im.utils.image-processing :as image-processing]
            [taoensso.timbre :as log]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.colors :as colors]))

(defn image-captured [data]
  (let [path       (.-uri data)
        _          (log/debug "Captured image: " path)
        on-success (fn [base64]
                     (log/debug "Captured success: " base64)
                     (re-frame/dispatch [:my-profile/update-picture base64])
                     (re-frame/dispatch [:navigate-back]))
        on-error   (fn [type error]
                     (log/debug type error))]
    (image-processing/img->base64 path on-success on-error 150 150)))

(defn profile-photo-capture []
  (let [camera-ref (reagent/atom nil)]
    [react/view styles/container
     [topbar/topbar {:title :t/image-source-title}]
     [camera/camera {:style         {:flex 1}
                     ;:aspect        (:fill camera/aspects)
                     :captureQuality "480p"
                     ;:captureTarget (:disk camera/capture-targets)
                     :type          "front"
                     :ref           #(reset! camera-ref %)}]
     [react/view styles/button-container
      [react/view styles/button
       [react/touchable-highlight {:on-press (fn []
                                               (let [camera @camera-ref]
                                                 (-> (.takePictureAsync camera)
                                                     (.then image-captured)
                                                     (.catch #(log/debug "Error capturing image: " %)))))}
        [react/view
         [icons/icon :main-icons/camera {:color colors/white}]]]]]]))
