(ns react-native.orientation
  (:require ["react-native-orientation-locker" :refer (useDeviceOrientationChange)]
            ["react-native-navigation" :refer (Navigation)]))


(def use-device-orientation-change useDeviceOrientationChange)

(defn lock-to-portrait
  []
  (.mergeOptions
   Navigation
   "lightbox"
   (clj->js {:layout    {:orientation ["portrait"]}
             :statusBar {:visible true}})))

(defn lock-to-landscape
  []
  (.mergeOptions
   Navigation
   "lightbox"
   (clj->js {:layout {:orientation ["landscape"]}}))
  ;; On Android, hiding the status-bar while changing orientation causes a flicker, so we enqueue it
  (js/setTimeout #(.mergeOptions
                   Navigation
                   "lightbox"
                   (clj->js {:statusBar {:visible false}}))
                 0))

(def portrait "PORTRAIT")

(def landscape "LANDSCAPE")

(def landscape-left "LANDSCAPE-LEFT")

(def landscape-right "LANDSCAPE-RIGHT")
