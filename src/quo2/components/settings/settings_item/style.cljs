(ns quo2.components.settings.settings-item.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [{:keys [container-style]}]
  (merge {:padding         12
          :flex-direction  :row
          :justify-content :space-between}
         container-style))

(defn left-sub-container
  [{:keys [tag description]}]
  {:flex-direction :row
   :align-items    (if (or tag description) :flex-start :center)})

(def sub-container
  {:flex-direction :row
   :align-items    :center})

(def left-container
  {:margin-left     12
   :height          "100%"
   :justify-content :flex-start})

(defn image-container
  [image tag description]
  {:height     (if (= image :icon-avatar) 32 20)
   :margin-top (if (or tag description) 1 0)})

(def status-container
  {:flex-direction :row
   :align-items    :center})

(defn status-dot
  [online? theme]
  {:width            8
   :height           8
   :border-radius    8
   :margin-right     6
   :background-color (if online?
                       (colors/theme-colors colors/success-50 colors/success-60 theme)
                       (colors/theme-colors colors/danger-50 colors/danger-60 theme))})

(defn color
  [blur? theme]
  {:color (if blur?
            colors/white-opa-70
            (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(defn label-dot
  [background-color]
  {:width            15
   :height           15
   :border-radius    12
   :background-color background-color})

(def status-tag-container
  {:margin-top    7
   :margin-bottom 2
   :margin-left   -1})
