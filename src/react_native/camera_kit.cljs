(ns react-native.camera-kit
  (:require ["react-native-camera-kit" :refer (CameraKitCamera)]
            [reagent.core :as reagent]))

(def camera (reagent/adapt-react-class CameraKitCamera))
