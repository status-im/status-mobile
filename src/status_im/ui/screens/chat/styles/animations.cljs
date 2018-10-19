(ns status-im.ui.screens.chat.styles.animations
  (:require [status-im.ui.components.styles :as common]
            [status-im.ui.components.colors :as colors]))

(def header-draggable-icon "rgba(73, 84, 93, 0.23)")

(def overlap-container
  {:position :absolute
   :left     0
   :top      0
   :right    0
   :bottom   0})

(defn expandable-container [anim-value bottom max-height]
  {:background-color colors/white
   :height           anim-value
   :left             0
   :right            0
   :bottom           bottom
   :position         :absolute
   :elevation        2
   :max-height       max-height})

(def header-container
  {:min-height       19
   :background-color colors/white
   :align-items      :center})

(def header-icon
  {:background-color header-draggable-icon
   :margin-top       8
   :margin-bottom    6
   :width            24
   :border-radius    1.5
   :height           3})
