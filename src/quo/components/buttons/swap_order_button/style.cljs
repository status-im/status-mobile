(ns quo.components.buttons.swap-order-button.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [pressed? disabled? theme]
  {:width            32
   :height           32
   :border-radius    10
   :border-width     1
   :opacity          (if disabled? 0.3 1)
   :align-items      :center
   :justify-content  :center
   :border-color     (if pressed?
                       (colors/theme-colors colors/neutral-20 colors/neutral-60 theme)
                       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))
   :background-color (if pressed?
                       (colors/theme-colors colors/neutral-5 colors/neutral-80 theme)
                       (colors/theme-colors colors/neutral-2_5 colors/neutral-90 theme))})
