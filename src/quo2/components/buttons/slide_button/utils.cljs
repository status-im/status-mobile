(ns quo2.components.buttons.slide-button.utils
  (:require
    [quo2.components.buttons.slide-button.constants :as constants]
    [quo2.foundations.colors :as colors]))

;; Utils
(defn slider-color
  "- `color-key`               `:main`/`:track`
   - `customization-color` Customization color"
  [color-key customization-color]
  (let [colors-by-key {:main  (colors/custom-color-by-theme customization-color 50 60)
                       :track (colors/custom-color-by-theme customization-color 50 60 10 10)}]
    (color-key colors-by-key)))

(defn clamp-value
  [value min-value max-value]
  (cond
    (< value min-value) min-value
    (> value max-value) max-value
    :else               value))

(defn calc-usable-track
  "Calculate the track section in which the
  thumb can move in. Mostly used for interpolations."
  [track-width thumb-size]
  (- track-width (* constants/track-padding 2) thumb-size))

(defn get-dimensions
  [track-width size dimension-key]
  (let [default-dimensions (case size
                             :small constants/small-dimensions
                             :large constants/large-dimensions
                             constants/large-dimensions)]
    (-> default-dimensions
        (merge {:usable-track (calc-usable-track
                               track-width
                               (:thumb default-dimensions))})
        (get dimension-key))))
