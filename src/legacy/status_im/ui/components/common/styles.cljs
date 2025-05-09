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

(def image-contain
  {:align-self      :stretch
   :align-items     :center
   :justify-content :center})
