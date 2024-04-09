(ns react-native.image-crop-picker
  (:require ["react-native-image-crop-picker" :default image-picker]))

(defn show-access-error
  [o]
  (js/console.log (.-message ^js o)))

(defn show-image-picker
  ([callback]
   (show-image-picker callback nil))
  ([callback
    {:keys [media-type]
     :or   {media-type "any"}
     :as   props}]
   (-> ^js image-picker
       (.openPicker (clj->js (merge {:mediaType media-type} props)))
       (.then #(callback (.-path ^js %)))
       (.catch show-access-error))))

(defn show-image-picker-camera
  ([callback]
   (show-image-picker-camera callback nil))
  ([callback props]
   (-> ^js image-picker
       (.openCamera (clj->js props))
       (.then #(callback (.-path ^js %)))
       (.catch show-access-error))))
