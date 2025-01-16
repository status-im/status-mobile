(ns quo.components.share.qr-code.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [size]
  (if size
    {:width  size
     :height size}
    {:flex 1}))

(def avatar-overlay
  {:justify-content :center
   :align-items     :center})

(defn qr-image
  [size]
  {:width            (or size "100%")
   :height           (or size "100%")
   :background-color colors/white-opa-70
   :border-radius    12
   :aspect-ratio     1})

(def ^:private avatar-container-common
  {:width            68
   :height           68
   :justify-content  :center
   :align-items      :center
   :background-color colors/white})

(def avatar-container-circular
  (assoc avatar-container-common :border-radius 33))

(def avatar-container-rounded
  (assoc avatar-container-common :border-radius 16))

(def big-avatar-container-rounded
  (assoc avatar-container-common
         :width         84
         :height        84
         :border-radius 42))

(def community-logo-image
  {:width         64
   :height        64
   :border-radius 32})
