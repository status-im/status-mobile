(ns react-native.camera-kit
  (:require ["react-native-camera-kit" :refer (Camera CameraType)]
            [reagent.core :as reagent]
            [taoensso.timbre :as log]))

(def camera (reagent/adapt-react-class Camera))

(def camera-type-front (.-Front CameraType))
(def camera-type-back (.-Back CameraType))

(defn capture
  [camera-ref callback]
  (-> (.capture ^js camera-ref)
      (.then callback)
      (.catch #(log/warn "couldn't capture photo"))))
