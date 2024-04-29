(ns status-im.common.image-crop-picker.events
  (:require [react-native.image-crop-picker :as image-crop-picker]
            [react-native.platform :as platform]
            [utils.re-frame :as rf]))

(rf/reg-fx :effect.image-crop-picker/show
 (fn [[callback crop-opts]]
   (when platform/ios?
     (rf/dispatch [:alert-banners/hide]))
   (image-crop-picker/show-image-picker
    callback
    crop-opts
    #(rf/dispatch [:alert-banners/unhide]))))

(rf/reg-fx :effect.image-crop-picker/show-camera
 (fn [[callback crop-opts]]
   (when platform/ios?
     (rf/dispatch [:alert-banners/hide]))
   (image-crop-picker/show-image-picker-camera
    callback
    crop-opts
    #(rf/dispatch [:alert-banners/unhide]))))

(rf/reg-event-fx
 :image-crop-picker/show
 (fn [_ [callback crop-opts]]
   {:effect.image-crop-picker/show [callback crop-opts]}))

(rf/reg-event-fx
 :image-crop-picker/show-camera
 (fn [_ [callback crop-opts]]
   {:effect.image-crop-picker/show-camera [callback crop-opts]}))
