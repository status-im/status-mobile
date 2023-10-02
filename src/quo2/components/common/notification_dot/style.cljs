(ns quo2.components.common.notification-dot.style
  (:require [quo2.foundations.colors :as colors]))

(def ^:const size 8)

(defn dot-background-color
  [customization-color theme blur?]
  (cond
    customization-color (colors/theme-colors
                         (colors/custom-color customization-color 50)
                         (colors/custom-color customization-color 60)
                         theme)
    blur?               (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
    :else               (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)))

(defn notification-dot
  [customization-color theme blur?]
  {:background-color (dot-background-color customization-color theme blur?)
   :width            size
   :height           size
   :border-radius    (/ size 2)
   :position         :absolute
   :z-index          1})
