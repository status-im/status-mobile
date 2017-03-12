(ns status-im.chat.styles.animations
  (:require [status-im.components.styles :as common]))

(def color-root-border "rgba(192, 198, 202, 0.28)")
(def header-draggable-icon "rgba(73, 84, 93, 0.23)")

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
  {:height           17
   :background-color common/color-white
   :alignItems       :center
   :justifyContent   :center})

(def header-icon
  {:background-color header-draggable-icon
   :width            24
   :border-radius    1.5
   :height           3})