(ns quo2.components.settings.settings-item.style
  (:require [quo2.foundations.colors :as colors]))

(defn find-icon-height
  [description tag image]
  (let [icon-height (if (= image :icon-avatar) 32 20)
        icon-height (if description 40 icon-height)]
    (if tag 72 icon-height)))

(defn container
  [{:keys [in-card? tag]}]
  {:padding-horizontal 12
   :padding-vertical   (if in-card? 12 13)
   :flex-direction     :row
   :justify-content    :space-between
   :height             (if tag 96 48)})

(def sub-container
  {:flex-direction :row
   :align-items    :center})

(def left-container
  {:margin-left     12
   :height          "100%"
   :justify-content :center})

(defn image-container
  [description tag image]
  {:height          (find-icon-height description tag image)
   :justify-content :flex-start})

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
