(ns react-native.orientation
  (:require ["react-native-orientation-locker" :refer (useDeviceOrientationChange)]
            ["react-native-navigation" :refer (Navigation)]))


(def use-device-orientation-change useDeviceOrientationChange)

(def portrait-options
  (clj->js {:layout    {:orientation ["portrait"]}
            :statusBar {:visible true}}))

(def landscape-option-1 (clj->js {:layout {:orientation ["landscape"]}}))
(def landscape-option-2 (clj->js {:statusBar {:visible false}}))

(defn lock-to-portrait
  []
  (.mergeOptions Navigation "lightbox" portrait-options))

(defn lock-to-landscape
  []
  (.mergeOptions Navigation "lightbox" landscape-option-1)
  ;; On Android, hiding the status-bar while changing orientation causes a flicker, so we enqueue it
  (js/setTimeout #(.mergeOptions Navigation "lightbox" landscape-option-2) 0))

(def portrait "PORTRAIT")

(def landscape "LANDSCAPE")

(def landscape-left "LANDSCAPE-LEFT")

(def landscape-right "LANDSCAPE-RIGHT")
