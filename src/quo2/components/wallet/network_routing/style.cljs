(ns quo2.components.wallet.network-routing.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defn container
  [container-style theme]
  (assoc container-style
         :flex-direction   :row
         :height           64
         :background-color (colors/theme-colors colors/neutral-100-opa-5 colors/neutral-90 theme)
         :border-radius    20
         :overflow         :hidden))

(defn max-limit-bar
  [{:keys [opacity-shared-value network-name width]}]
  (reanimated/apply-animations-to-style
   {:opacity opacity-shared-value}
   {:position           :absolute
    :top                0
    :bottom             0
    :left               0
    :background-color   (colors/custom-color network-name nil 10)
    :width              width
    :border-right-width 1
    :border-style       :dashed
    :border-right-color (colors/custom-color network-name)
    :z-index            -1}))

(defn network-bar
  [{:keys                                           [max-width on-top? bar-division? theme]
    {:keys [network-name translate-x-shared-value]} :bar}
   width-shared-value]
  (reanimated/apply-animations-to-style
   {:width     width-shared-value
    :transform [{:translate-x translate-x-shared-value}]}
   {:max-width          max-width
    :flex-direction     :row
    :justify-content    :flex-end
    :background-color   (colors/custom-color network-name)
    :z-index            (if on-top? 1 0)
    :border-right-width (if bar-division? 0 1)
    :border-color       (colors/theme-colors colors/white colors/neutral-95 theme)}))

(def slider-container
  {:width            40
   :background-color :transparent
   :justify-content  :center
   :align-items      :center
   :right            -20})

(def ^:private slider-fixed-styles
  {:background-color colors/white
   :height           32
   :width            4
   :border-radius    4})

(defn slider
  [{:keys [width-shared-value height-shared-value opacity-shared-value]}]
  (reanimated/apply-animations-to-style
   {:width   width-shared-value
    :height  height-shared-value
    :opacity opacity-shared-value}
   slider-fixed-styles))
