(ns quo2.components.buttons.slide-button.utils
  (:require
    [quo2.components.buttons.slide-button.constants :as constants]
    [quo2.foundations.colors :as colors]))

(defn main-color
  "`customization-color` Customization color"
  [customization-color theme]
  (colors/theme-colors
   (colors/custom-color customization-color 50)
   (colors/custom-color customization-color 60)
   theme))

(defn track-color
  "`customization-color` Customization color"
  ([customization-color blur?]
   (if blur?
     colors/white-opa-5
     (colors/custom-color customization-color 50 10))))

(defn text-color
  "`customization-color` Customization color"
  [customization-color theme blur?]
  (if blur?
    colors/white-opa-40
    (colors/theme-colors
     (colors/custom-color customization-color 50)
     (colors/custom-color customization-color 60)
     theme)))

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
                             :size-40 constants/small-dimensions
                             :size-48 constants/large-dimensions
                             constants/large-dimensions)]
    (-> default-dimensions
        (merge {:usable-track (calc-usable-track
                               track-width
                               (:thumb default-dimensions))})
        (get dimension-key))))
