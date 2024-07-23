(ns quo.foundations.shadows
  (:refer-clojure :exclude [get])
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]
    [utils.number]))

;; Set radius coefficient based on Android Version.
;; A good way to know is to check if shadow color is supported i.e API level 28 (Android 9) above
(def ^:private shadow-radius-coefficient (if platform/is-shadow-color-supported? 0.8 0.1))

(defn- get-shadow-color
  "Calculates the shadow color based on a given color and opacity, with an optional coefficient.
  - Arity [color opacity]: Uses a default coefficient value of 5 (empirically determined) to compute the shadow color.
  - Arity [color opacity coefficient]: Uses the specified coefficient to compute the shadow color.
  On Android platforms, the shadow color's alpha is adjusted based on the product of opacity and
  coefficient, constrained between 0 and 1. Returns a map with :shadow-color and :shadow-opacity keys."
  ([color opacity]
   (get-shadow-color color opacity 5))
  ([color opacity coefficient]
   {:shadow-color   (if platform/android?
                      (colors/alpha color (utils.number/value-in-range (* opacity coefficient) 0 1))
                      color)
    :shadow-opacity opacity}))

(defn- get-shadow-radius
  "Computes shadow radius and elevation properties based on a given radius, with an optional coefficient.
  - Arity [radius]: Uses a default coefficient of 0.8 for API level 28 and above and 0.1 otherwise (empirically determined) to compute the elevation.
  - Arity [radius coefficient]: Uses the specified coefficient to compute the elevation.
  Returns a map with :elevation, computed as the product of radius and coefficient, and :shadow-radius keys."
  ([radius]
   (get-shadow-radius radius shadow-radius-coefficient))
  ([radius coefficient]
   {:elevation     (* radius coefficient)
    :shadow-radius radius}))

(def ^:private shadows
  (let [dark-normal           {1 (merge {:shadow-offset {:width 0 :height 4}}
                                        (get-shadow-color colors/neutral-100 0.5)
                                        (get-shadow-radius 20))
                               2 (merge {:shadow-offset {:width 0 :height 4}}
                                        (get-shadow-color colors/neutral-100 0.64)
                                        (get-shadow-radius 20))
                               3 (merge {:shadow-offset {:width 0 :height 12}}
                                        (get-shadow-color colors/neutral-100 0.64)
                                        (get-shadow-radius 20))
                               4 (merge {:shadow-offset {:width 0 :height 16}}
                                        (get-shadow-color colors/neutral-100 0.72)
                                        (get-shadow-radius 20))}
        dark-normal-inverted  (-> dark-normal
                                  (update-in [:soft :shadow-offset :height] -)
                                  (update-in [:medium :shadow-offset :height] -)
                                  (update-in [:intense :shadow-offset :height] -)
                                  (update-in [:strong :shadow-offset :height] -))
        light-normal          {1 (merge {:shadow-offset {:width 0 :height 4}}
                                        (get-shadow-color colors/neutral-100 0.04)
                                        (get-shadow-radius 16))

                               2 (merge {:shadow-offset {:width 0 :height 4}}
                                        (get-shadow-color colors/neutral-100 0.08)
                                        (get-shadow-radius 16))
                               3 (merge {:shadow-offset {:width 0 :height 12}}
                                        (get-shadow-color colors/neutral-100 0.12)
                                        (get-shadow-radius 16))
                               4 (merge {:shadow-offset {:width 0 :height 16}}
                                        (get-shadow-color colors/neutral-100 0.16)
                                        (get-shadow-radius 16))}
        light-normal-inverted (-> light-normal
                                  (update-in [:soft :shadow-offset :height] -)
                                  (update-in [:medium :shadow-offset :height] -)
                                  (update-in [:intense :shadow-offset :height] -)
                                  (update-in [:strong :shadow-offset :height] -))]
    {:dark  {:normal dark-normal :inverted dark-normal-inverted}
     :light {:normal light-normal :inverted light-normal-inverted}}))

(defn get
  "Get the appropriate shadow map for a given shadow `weight`, `theme`, and `scale-type`.

  Return nil if no shadow is found.

  `weight` - int (required) from 1 to 4.
  `theme` - :light/:dark (optional).
  `scale-type` - :normal/:inverted (optional).
  "
  ([weight theme]
   (get weight theme :normal))
  ([weight theme scale-type]
   (get-in shadows [theme scale-type weight])))

(def inner-shadow
  {:shadow-color   (colors/alpha colors/neutral-100 0.08)
   :shadow-offset  {:width  0
                    :height 0}
   :shadow-opacity 1
   :shadow-radius  16
   :elevation      13})
