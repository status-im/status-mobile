(ns legacy.status-im.ui.screens.profile.user.edit-picture
  (:require
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [status-im2.config :as config]
    [status-im2.contexts.profile.settings.events]
    [utils.i18n :as i18n]))

(def crop-size 1000)
(def crop-opts
  {:cropping             true
   :cropperCircleOverlay true
   :width                crop-size
   :height               crop-size})

(defn pick-pic
  []
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (react/show-image-picker
   #(re-frame/dispatch [:profile.settings/save-profile-picture (.-path ^js %) 0 0 crop-size crop-size])
   crop-opts))

(defn take-pic
  []
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (react/show-image-picker-camera
   #(re-frame/dispatch [:profile.settings/save-profile-picture (.-path ^js %) 0 0 crop-size crop-size])
   crop-opts))

(defn bottom-sheet
  [has-picture]
  (fn []
    [:<>
     [list.item/list-item
      {:accessibility-label :take-photo
       :theme               :accent
       :icon                :main-icons/camera
       :title               (i18n/label :t/profile-pic-take)
       :on-press            take-pic}]
     [list.item/list-item
      {:accessibility-label :pick-photo
       :icon                :main-icons/gallery
       :theme               :accent
       :title               (i18n/label :t/profile-pic-pick)
       :on-press            pick-pic}]
     (when (and config/enable-remove-profile-picture?
                has-picture)
       [list.item/list-item
        {:accessibility-label :remove-photo
         :icon                :main-icons/delete
         :theme               :accent
         :title               (i18n/label :t/profile-pic-remove)
         :on-press            #(re-frame/dispatch [:profile.settings/delete-profile-picture nil])}])]))
