(ns quo2.components.buttons.slide-button.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defn slide-container
  [slider-height slider-padding slider-width disabled]
  {:background-color "#101B3A"
   :height           slider-height
   :padding          slider-padding
   :width            slider-width
   :border-radius    14
   :opacity          (when disabled 0.5)
   :justify-content  :center
   :align-items      :center})

(defn foreground-pallet
  [translate-x knob-width slider-padding]
  (reanimated/apply-animations-to-style
   {:width translate-x}
   {:z-index          1
    :position         :absolute
    :left             0
    :top              0
    :border-radius    14
    :overflow         :hidden
    :background-color "#101B3A"
    :height           (+ knob-width (* slider-padding 2))}))

(defn knob
  [knob-width slider-padding]
  {:width            knob-width
   :height           knob-width
   :position         :absolute
   :align-items      :center
   :justify-content  :center
   :right            0
   :top              slider-padding
   :border-radius    10
   :background-color (colors/theme-colors
                      colors/primary-60
                      colors/primary-50)})
(def text
  {:color       colors/primary-50
   :margin-left 4})

(def knob-icon
  {:flex-direction  :row
   :align-items     :center
   :justify-content :center})