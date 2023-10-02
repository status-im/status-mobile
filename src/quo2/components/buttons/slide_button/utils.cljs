(ns quo2.components.buttons.slide-button.utils
  (:require
    [quo2.components.buttons.slide-button.constants :as constants]
    [quo2.foundations.colors :as colors]))

(defn slider-color
  "- `color-key`               `:main`/`:track`
   - `customization-color` Customization color"
  [color-key customization-color theme]
  (let [colors-by-key {:main  (colors/theme-colors
                               (colors/custom-color customization-color 50)
                               (colors/custom-color customization-color 60)
                               theme)
                       :track (colors/theme-colors
                               (colors/custom-color customization-color 50 10)
                               (colors/custom-color customization-color 60 10)
                               theme)}]
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
  (let [double-padding (* constants/track-padding 2)]
    (- track-width double-padding thumb-size)))

(defn get-dimensions
  [track-width size dimension-key]
  (let [default-dimensions (case size
                             :size/s-40 constants/small-dimensions
                             :size/s-48 constants/large-dimensions
                             constants/large-dimensions)]
    (-> default-dimensions
        (merge {:usable-track (calc-usable-track
                               track-width
                               (:thumb default-dimensions))})
        (get dimension-key))))
