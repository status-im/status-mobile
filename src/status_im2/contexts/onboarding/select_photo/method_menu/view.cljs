(ns status-im2.contexts.onboarding.select-photo.method-menu.view
  (:require
    [quo2.core :as quo]
    [utils.i18n :as i18n]
    ["react-native-image-crop-picker" :default image-picker]
    [status-im.multiaccounts.core]
    [utils.re-frame :as rf]
    [react-native.permissions :as permissions]))

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

  (permissions/permission-granted?
   :read-external-storage
   (fn [granted]
     (if granted
       (show-image-picker
        #(update-profile-pic-callback (.-path ^js %))
        crop-opts)
       (permissions/request-permissions
        {:permissions [:read-external-storage]
         :on-allowed  (fn []
                        (show-image-picker
                         #(update-profile-pic-callback (.-path ^js %))
                         crop-opts))
         :on-denied   (fn [error]
                        (js/console.log "Permission denied"))})))
   (fn [error]
     (js/console.log "Error checking permission:" error))))

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
      :on-press            #(take-pic update-profile-pic-callback)}
     {:icon                :i/image
      :accessibility-label :select-from-gallery-button
      :label               (i18n/label :t/profile-pic-pick)
      :on-press            #(pick-pic update-profile-pic-callback)}]]])
