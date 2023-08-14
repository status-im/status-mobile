(ns quo2.components.common.notification-dot.style
  (:require [quo2.foundations.colors :as colors]))

(def ^:const size 8)

(defn dot-background-color
  [customization-color theme]
  (if customization-color
    (colors/theme-colors
     (colors/custom-color customization-color 50)
     (colors/custom-color customization-color 60)
     theme)
    (colors/theme-colors colors/primary-50 colors/primary-60 theme)))

(defn notification-dot
  [customization-color theme]
  {:background-color (dot-background-color customization-color theme)
   :width            size
   :height           size
   :border-radius    (/ size 2)
   :position         :absolute
   :z-index          1})
