(ns status-im.chat.styles.animations
  (:require [status-im.ui.components.styles :as common]))

(def color-root-border "rgba(192, 198, 202, 0.28)")
(def header-draggable-icon "rgba(73, 84, 93, 0.23)")

(defn result-box-overlay [max-height opacity-anim-value]
  {:background-color common/color-black
   :position         :absolute
   :opacity          opacity-anim-value
   :height           max-height
   :elevation        2
   :bottom           0
   :left             0
   :right            0})

(def overlap-container
  {:position :absolute
   :left     0
   :top      0
   :right    0
   :bottom   0})

(defn expandable-container [anim-value bottom]
  {:background-color common/color-white
   :border-top-color color-root-border
   :border-top-width 1
   :elevation        2
   :height           anim-value
   :left             0
   :right            0
   :bottom           bottom
   :position         :absolute})

(def header-container
  {:min-height       19
   :background-color common/color-white
   :alignItems       :center})

(def header-icon
  {:background-color header-draggable-icon
   :margin-top       8
   :margin-bottom    6
   :width            24
   :border-radius    1.5
   :height           3})
