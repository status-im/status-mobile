(ns quo.components.profile.expanded-collectible.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [aspect-ratio]
  {:align-items     :center
   :justify-content :center
   :border-radius   16
   :width           "100%"
   :aspect-ratio    aspect-ratio})

(defn image
  [square? aspect-ratio theme]
  {:width            "100%"
   :aspect-ratio     (if square? 1 aspect-ratio)
   :border-radius    16
   :background-color (colors/theme-colors colors/white colors/neutral-95 theme)})

(defn collectible-border
  [theme]
  {:position      :absolute
   :top           0
   :left          0
   :right         0
   :bottom        0
   :border-radius 16
   :border-width  1
   :border-color  (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)})

(defn fallback
  [{:keys [theme]} aspect-ratio]
  {:background-color (colors/theme-colors colors/neutral-2_5 colors/neutral-90 theme)
   :border-style     :dashed
   :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :border-width     1
   :border-radius    16
   :width            "100%"
   :aspect-ratio     aspect-ratio
   :align-items      :center
   :justify-content  :center})

(def counter
  {:position :absolute
   :top      12
   :right    12})

(defn loading-image-with-opacity
  [theme opacity]
  [{:opacity  opacity
    :position :absolute
    :z-index  1}
   (fallback {:theme theme} 1)])

(defn supported-file
  [opacity]
  {:opacity opacity})
