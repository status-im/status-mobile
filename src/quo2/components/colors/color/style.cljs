(ns quo2.components.colors.color.style
  (:require
    [quo2.foundations.colors :as colors]))

(def color-button-common
  {:width             48
   :height            48
   :border-width      4
   :border-radius     24
   :margin-horizontal 4
   :transform         [{:rotate "45deg"}]
   :border-color      :transparent})

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

(defn feng-shui
  [theme]
  {:width            40
   :height           40
   :transform        [{:rotate "45deg"}]
   :overflow         :hidden
   :border-color     (colors/theme-colors colors/neutral-100 colors/white theme)
   :border-width     2
   :background-color (colors/theme-colors colors/neutral-100 colors/white theme)
   :border-radius    20})

(defn left-half
  [theme]
  {:flex             1
   :background-color (colors/theme-colors colors/white colors/neutral-100 theme)})

(defn right-half
  [theme]
  {:flex             1
   :background-color (colors/theme-colors colors/neutral-100 colors/white theme)})
