(ns legacy.status-im.ui.components.common.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(defn logo-container
  [size]
  {:width            size
   :height           size
   :border-radius    size
   :background-color colors/blue
   :align-items      :center
   :justify-content  :center})

(defn logo
  [icon-size]
  {:width           icon-size
   :height          icon-size
   :color           :none
   :container-style {}})

(defn counter-container
  [size]
  {:width            size
   :height           size
   :border-radius    (/ size 2)
   :background-color colors/blue
   :align-items      :center
   :justify-content  :center})

(defn counter-label
  [size]
  {:font-size  (inc (/ size 2))
   :typography :main-medium
   :color      colors/white-persist
   :text-align :center})

(def image-contain
  {:align-self      :stretch
   :align-items     :center
   :justify-content :center})
