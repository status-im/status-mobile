(ns quo2.components.switchers.style
  (:require [quo2.foundations.colors :as colors]))

(def title
  {:margin-top 28
   :color      colors/white})

(def subtitle
  {:color colors/neutral-40})

(def avatar-container
  {:left          10
   :top           -30
   :border-radius 48
   :border-width  2
   :border-color  colors/neutral-95
   :position      :absolute})

(def content-container
  {:position        :absolute
   :left            12
   :right           12
   :bottom          12
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def notification-container
  {:margin-top      :auto
   :width           20
   :height          20
   :justify-content :center
   :align-items     :center
   :margin-left     8})

(def last-message-text
  {:color colors/white})

(def sticker
  {:width  24
   :height 24})

(def gif
  {:width         24
   :height        24
   :border-radius 8})
