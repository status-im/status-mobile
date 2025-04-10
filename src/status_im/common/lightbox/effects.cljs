(ns status-im.common.lightbox.effects
  (:require [react-native.blob :as blob]
            [react-native.cameraroll :as cameraroll]
            [react-native.fs :as fs]
            [react-native.platform :as platform]
            [utils.re-frame :as rf]))

(def config
  {:trusty platform/ios?
   :path   (str (fs/cache-dir) "/StatusIm_Image.jpeg")})

(rf/reg-fx :effects.lightbox/share-image
 (fn [uri]
   (blob/fetch uri
               config
               (fn [downloaded-url]
                 (rf/dispatch [:open-share
                               {:options    {:url       (str (when platform/android? "file://")
                                                             downloaded-url)
                                             :isNewTask true}
                                :on-success #(fs/unlink downloaded-url)
                                :on-error   #(fs/unlink downloaded-url)}])))))

(rf/reg-fx :effects.lightbox/save-image-to-gallery
 (fn [[uri on-success]]
   (blob/fetch uri
               config
               (fn [downloaded-url]
                 (cameraroll/save-image downloaded-url)
                 (on-success)))))
