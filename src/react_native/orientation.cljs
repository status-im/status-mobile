(ns react-native.orientation
  (:require ["react-native-orientation-locker" :default orientation :refer (useDeviceOrientationChange)]
            [react-native.navigation :as navigation]))

(def use-device-orientation-change useDeviceOrientationChange)

(def get-auto-rotate-state (.-getAutoRotateState orientation))

(def portrait-options
  (clj->js {:layout    {:orientation ["portrait"]}
            :statusBar {:visible true}}))

(def landscape-option-1 (clj->js {:layout {:orientation ["landscape"]}}))
(def landscape-option-2 (clj->js {:statusBar {:visible false}}))

(defn lock-to-portrait
  [id]
  (navigation/merge-options id portrait-options))

(defn lock-to-landscape
  [id]
  (navigation/merge-options id landscape-option-1)
  ;; On Android, hiding the status-bar while changing orientation causes a flicker, so we enqueue it
  (js/setTimeout #(navigation/merge-options id landscape-option-2) 0))

(def ^:const portrait "PORTRAIT")

(def ^:const landscape "LANDSCAPE")

(def ^:const landscape-left "LANDSCAPE-LEFT")

(def ^:const landscape-right "LANDSCAPE-RIGHT")
