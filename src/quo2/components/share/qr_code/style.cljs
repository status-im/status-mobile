(ns quo2.components.share.qr-code.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [size]
  (if size
    {:width  size
     :height size}
    {:flex 1}))

(def avatar-overlay
  {:position        :absolute
   :top             0
   :right           0
   :left            0
   :bottom          0
   :justify-content :center
   :align-items     :center})

(defn qr-image
  [size]
  {:width            (or size "100%")
   :height           (or size "100%")
   :background-color colors/white-opa-70
   :border-radius    12
   :aspect-ratio     1})

(def ^:private avatar-container-common
  {:width            66
   :height           66
   :justify-content  :center
   :align-items      :center
   :background-color colors/white})

(def avatar-container-circular
  (assoc avatar-container-common :border-radius 33))

(def avatar-container-rounded
  (assoc avatar-container-common :border-radius 16))

(def community-logo-image
  {:width         64
   :height        64
   :border-radius 32})
