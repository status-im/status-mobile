(ns quo2.components.colors.color-picker.style
  (:require [quo2.foundations.colors :as colors]))

(def color-picker-container
  {:flex            1
   :flex-direction  :row
   :flex-wrap       :wrap
   :justify-content :space-between})

(def flex-break
  {:flex-basis "100%"
   :height     10})

(def color-button-common
  {:width         48
   :height        48
   :border-width  4
   :border-radius 24
   :transform     [{:rotate "45deg"}]
   :border-color  :transparent})

(defn color-button
  [color selected?]
  (merge color-button-common
         (when selected?
           {:border-top-color    (colors/alpha color 0.4)
            :border-end-color    (colors/alpha color 0.4)
            :border-bottom-color (colors/alpha color 0.2)
            :border-start-color  (colors/alpha color 0.2)})))

(defn color-circle
  [color border?]
  {:width            40
   :height           40
   :transform        [{:rotate "-45deg"}]
   :background-color color
   :justify-content  :center
   :align-items      :center
   :border-color     color
   :border-width     (if border? 2 0)
   :overflow         :hidden
   :border-radius    20})

(defn secondary-overlay
  [secondary-color border?]
  {:width            (if border? 18 20)
   :height           40
   :position         :absolute
   :right            0
   :background-color secondary-color})

