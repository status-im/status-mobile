(ns react-native.camera-kit
  (:require ["react-native-camera-kit" :refer (Camera CameraType)]
            [reagent.core :as reagent]))

(def camera (reagent/adapt-react-class Camera))

(def camera-type-front (.-Front CameraType))
(def camera-type-back (.-Back CameraType))
