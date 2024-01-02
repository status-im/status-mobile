(ns status-im.contexts.onboarding.select-photo.method-menu.view
  (:require
    ["react-native-image-crop-picker" :default image-picker]
    [quo.core :as quo]
    [react-native.permissions :as permissions]
    [react-native.platform :as platform]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def crop-size 1000)

(def crop-opts
  {:cropping             true
   :cropperCircleOverlay true
   :width                crop-size
   :height               crop-size})

(defn show-access-error
  [o]
  (when (= "E_PERMISSION_MISSING" (.-code ^js o))
    (js/console.log (i18n/label :t/error))))

(defn show-image-picker
  ([images-fn]
   (show-image-picker images-fn nil))
  ([images-fn
    {:keys [media-type]
     :or   {media-type "any"}
     :as   props}]
   (-> ^js image-picker
       (.openPicker (clj->js (merge {:mediaType media-type}
                                    props)))
       (.then images-fn)
       (.catch show-access-error))))

(defn show-image-picker-camera
  ([images-fn]
   (show-image-picker-camera images-fn nil))
  ([images-fn props]

   (-> ^js image-picker
       (.openCamera (clj->js props))
       (.then images-fn)
       (.catch show-access-error))))

(defn pick-pic
  [update-profile-pic-callback]
  (rf/dispatch [:hide-bottom-sheet])
  (show-image-picker
   #(update-profile-pic-callback (.-path ^js %))
   crop-opts))

(defn take-pic
  [update-profile-pic-callback]
  (rf/dispatch [:hide-bottom-sheet])
  (show-image-picker-camera
   #(update-profile-pic-callback (.-path ^js %))
   crop-opts))

(defn view
  [update-profile-pic-callback]
  [quo/action-drawer
   [[{:icon                :i/camera
      :accessibility-label :take-photo-button
      :label               (i18n/label :t/profile-pic-take)
      :on-press            (fn []
                             (permissions/request-permissions
                              {:permissions [(if platform/is-below-android-13?
                                               :read-external-storage
                                               :read-media-images)
                                             :write-external-storage]
                               :on-allowed  (fn [] (take-pic update-profile-pic-callback))
                               :on-denied   (fn []
                                              (log/info
                                               "user has denied permissions to click picture"))}))}
     {:icon                :i/image
      :accessibility-label :select-from-gallery-button
      :label               (i18n/label :t/profile-pic-pick)
      :on-press            (fn []
                             (permissions/request-permissions
                              {:permissions [(if platform/is-below-android-13?
                                               :read-external-storage
                                               :read-media-images)
                                             :write-external-storage]
                               :on-allowed  (fn [] (pick-pic update-profile-pic-callback))
                               :on-denied   (fn []
                                              (log/info
                                               "user has denied permissions to select picture"))}))}]]])

