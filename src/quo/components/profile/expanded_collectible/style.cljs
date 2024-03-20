(ns quo.components.profile.expanded-collectible.style
  (:require [quo.foundations.colors :as colors]
            [quo.foundations.shadows :as shadows]))

(def container
  {:flex            1
   :align-items     :center
   :justify-content :center
   :border-radius   16})

(defn image
  [square? aspect-ratio]
  (merge (shadows/get 2)
         {:width         "100%"
          :border-width  1
          :aspect-ratio  (if square? 1 aspect-ratio)
          :border-radius 16}
         (when false {:flex 1})))

(defn fallback
  [{:keys [theme]}]
  {:background-color (colors/theme-colors colors/neutral-2_5 colors/neutral-90 theme)
   :border-style     :dashed
   :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :border-width     1
   :border-radius    16
   :width            "100%"
   :aspect-ratio     1
   :align-items      :center
   :justify-content  :center})

(def counter
  {:position :absolute
   :top      12
   :right    12})
