(ns react-native.camera-kit
  (:require ["react-native-camera-kit" :refer (Camera CameraType)]
            [reagent.core :as reagent]
            [oops.core :as oops]
            [taoensso.timbre :as log]))

(def camera (reagent/adapt-react-class Camera))

(def camera-type-front (.-Front CameraType))
(def camera-type-back (.-Back CameraType))

(defn capture
  [^js camera-ref on-success]
  (-> (.capture camera-ref)
      (.then #(on-success (oops/oget % :uri)))
      (.catch #(log/warn "couldn't capture photo" {:error %}))))
