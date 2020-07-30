(ns status-im.ui.components.camera
  (:require [reagent.core :as reagent]
            [clojure.string :as string]
            ["react-native-camera-kit" :refer (CameraKitCamera)]))

(def camera (reagent/adapt-react-class CameraKitCamera))

(defn get-qr-code-data [^js event]
  (when-let [data (-> event .-nativeEvent .-codeStringValue)]
    (string/trim data)))