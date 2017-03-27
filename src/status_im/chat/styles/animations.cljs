(ns status-im.chat.styles.animations
  (:require [status-im.components.styles :as common]))

(def color-root-border "rgba(192, 198, 202, 0.5)")

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
  {:height           22
   :background-color common/color-white
   :alignItems       :center
   :justifyContent   :center})

(def header-icon
  {:background-color "#bbbbbb"
   :width            30
   :border-radius    2
   :height           3})