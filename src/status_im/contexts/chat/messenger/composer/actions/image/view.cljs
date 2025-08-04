(ns status-im.contexts.chat.messenger.composer.actions.image.view
  (:require [quo.core :as quo]
            [react-native.permissions :as permissions]
            [react-native.platform :as platform]
            [status-im.common.alert.effects :as alert.effects]
            [status-im.common.device-permissions :as device-permissions]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn photo-limit-toast
  []
  (rf/dispatch [:toasts/upsert
                {:id   :random-id
                 :type :negative
                 :text (i18n/label :t/hit-photos-limit
                                   {:max-photos constants/max-album-photos})}]))

(defn go-to-camera
  [images-count]
  (device-permissions/camera #(if (>= images-count constants/max-album-photos)
                                (photo-limit-toast)
                                (rf/dispatch [:navigate-to :screen/camera-screen]))))

(defn camera-button
  []
  (let [images-count (count (vals (rf/sub [:chats/sending-image])))]
    [quo/composer-button
     {:on-press            #(go-to-camera images-count)
      :accessibility-label :camera-button
      :icon                :i/camera
      :container-style     {:margin-right 12}}]))

(defn open-photo-selector
  [input-ref]
  (permissions/request-permissions
   {:permissions [(if platform/is-below-android-13? :read-external-storage :read-media-images)
                  :write-external-storage]
    :on-allowed  (fn []
                   (when (and platform/android? @input-ref)
                     (.blur ^js @input-ref))
                   (when platform/ios?
                     (rf/dispatch [:alert-banners/hide]))
                   (rf/dispatch [:photo-selector/navigate-to-photo-selector]))
    :on-denied   #(alert.effects/show-popup (i18n/label :t/error)
                                            (i18n/label :t/external-storage-denied))}))

(defn image-button
  [input-ref]
  [quo/composer-button
   {:on-press            #(open-photo-selector input-ref)
    :accessibility-label :open-images-button
    :container-style     {:margin-right 12}
    :icon                :i/image}])
