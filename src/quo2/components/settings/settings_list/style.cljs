(ns quo2.components.settings.settings-list.style
  (:require [quo2.foundations.colors :as colors]))

(def title-container
  {:flex 1})

(defn title
  []
  {:color (colors/theme-colors
           colors/neutral-100
           colors/white)})

(def icon
  {:margin-right 12})

(def item-container
  {:justify-content    :space-between
   :height             48
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 12
   :padding-vertical   13})

(defn dot
  []
  {:width            15
   :height           15
   :border-radius    8
   :margin-right     14.5
   :background-color (colors/theme-colors (colors/custom-color :blue 50)
                                          (colors/custom-color :blue 60))})

(defn community-icon
  [index]
  {:width         24
   :height        24
   :border-width  1
   :border-color  (colors/theme-colors colors/white colors/black)
   :border-radius 12
   :position      :absolute
   :top           "-50%"
   :right         (* 20 index)})

(def communities-container
  {:flex         1
   :margin-right 12})
