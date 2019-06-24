(ns status-im.ui.screens.chat.styles.animations
  (:require [status-im.ui.components.styles :as common]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]))

(def header-draggable-icon "rgba(73, 84, 93, 0.23)")

(def overlap-container
  {:position :absolute
   :left     0
   :top      0
   :right    0
   :bottom   0})

(defn expandable-container [anim-value keyboard-height max-height]
  {:background-color colors/white
   :left             0
   :right            0
   :bottom           (if platform/ios?
                       (- keyboard-height 72)
                       0)
   :position         :absolute
   :elevation        2
   :shadow-offset    {:width 0 :height 1}
   :shadow-radius    12
   :shadow-opacity   0.12
   :shadow-color     colors/white
   :max-height       max-height
   :transform        [{:translateY anim-value}]})

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
