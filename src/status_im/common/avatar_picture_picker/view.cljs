(ns status-im.common.avatar-picture-picker.view
  (:require
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

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch event))

(defn view
  [{:keys [on-result has-picture?]}]
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
                               :on-allowed  #(hide-sheet-and-dispatch [:image-crop-picker/show-camera
                                                                       on-result crop-opts])
                               :on-denied   #(log/info
                                              "user has denied permissions to click picture")}))}
     {:icon                :i/image
      :accessibility-label :select-from-gallery-button
      :label               (i18n/label :t/profile-pic-pick)
      :on-press            (fn []
                             (permissions/request-permissions
                              {:permissions [(if platform/is-below-android-13?
                                               :read-external-storage
                                               :read-media-images)
                                             :write-external-storage]
                               :on-allowed  #(hide-sheet-and-dispatch [:image-crop-picker/show on-result
                                                                       crop-opts])
                               :on-denied   #(log/info
                                              "user has denied permissions to select picture")}))}
     (when has-picture?
       {:accessibility-label :remove-profile-picture
        :add-divider?        true
        :danger?             true
        :blur?               true
        :icon                :i/delete
        :label               (i18n/label :t/profile-pic-remove)
        :on-press            (fn []
                               (rf/dispatch [:hide-bottom-sheet])
                               (on-result nil))})]]])
