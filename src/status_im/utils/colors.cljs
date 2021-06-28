(ns status-im.utils.colors
  (:require [clojure.string :as string]))

(defn hexToRgb
  [hex]
  (let [shorthandRegex #"(?i)^#?([a-f\d])([a-f\d])([a-f\d])$"
        rgbColor
          (string/replace
            hex
            shorthandRegex
            (fn [m r g b] (+ (+ (+ (+ (+ r r) g) g) b) b)))
        result (.exec #"(?i)^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$" rgbColor)]
        (if result [
          (js/parseInt (aget result 1) 16)
           (js/parseInt (aget result 2) 16)
            (js/parseInt (aget result 3) 16)] nil)))

(defn color-brightness
  [color]
  (
    let [[r g b] (hexToRgb color)]
    (def targetColor (str "rgb(" r "," g "," b ")"))
    (def colors (.match targetColor #"^rgb\((\d+),\s*(\d+),\s*(\d+)\)$"))
    (def brightness
      (.round
        js/Math
        (/
          (+
            (+
              (* (js/parseInt (aget colors 1)) 299)
              (* (js/parseInt (aget colors 2)) 587))
            (* (js/parseInt (aget colors 3)) 114))
          1000)))
  brightness))


