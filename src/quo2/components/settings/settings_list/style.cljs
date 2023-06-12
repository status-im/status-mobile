(ns quo2.components.settings.settings-list.style
  (:require [quo2.foundations.colors :as colors]))

(def title-container
  {:flex 1})

(defn title
  [override-theme]
  {:color (colors/theme-colors
           colors/neutral-100
           colors/white
           override-theme)})

(def icon
  {:margin-right 12
   :align-self   :flex-start})

(def item-container
  {:justify-content    :space-between
   :align-items        :center
   :padding-horizontal 12
   :padding-vertical   13})

(def border-style
  {
 :border-color       (colors/theme-colors colors/neutral-20 colors/neutral-80)
 :border-width       0.5
 :overflow :hidden
 })
(def inner-container
  {:flex-direction :row
   :align-items    :center})

(defn dot
  [override-theme]
  {:width            15
   :height           15
   :border-radius    8
   :margin-right     14.5
   :background-color (colors/theme-colors (colors/custom-color :blue 50)
                                          (colors/custom-color :blue 60)
                                          override-theme)})

(defn community-icon
  [index override-theme]
  {:width         24
   :height        24
   :border-width  1
   :border-color  (colors/theme-colors colors/white colors/black override-theme)
   :border-radius 12
   :position      :absolute
   :right         (* 20 index)})

(def communities-container
  {:flex            1
   :justify-content :center
   :align-content   :center
   :margin-right    12})

(def tag-container
  {:margin-top 8})
