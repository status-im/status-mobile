(ns status-im2.contexts.chat.lightbox.effects
  (:require [react-native.blob :as blob]
            [react-native.cameraroll :as cameraroll]
            [react-native.fs :as fs]
            [react-native.platform :as platform]
            [react-native.share :as share]
            [utils.re-frame :as rf]))

(rf/reg-fx :effects.chat/share-image
 (fn [uri]
   (blob/get uri
             platform/ios?
             (fn [downloaded-url]
               (share/open {:url       (str (when platform/android? "file://") downloaded-url)
                            :isNewTask true}
                           #(fs/unlink downloaded-url)
                           #(fs/unlink downloaded-url))))))

(rf/reg-fx :effects.chat/save-image-to-gallery
 (fn [[uri on-success]]
   (blob/get uri
             platform/ios?
             (fn [downloaded-url]
               (cameraroll/save-image downloaded-url)
               (on-success)))))
