(ns status-im.chat.styles.input.result-box
  (:require [status-im.components.styles :as common]))

(def color-root-border "rgba(192, 198, 202, 0.5)")

(defn root [height bottom]
  {:background-color common/color-white
   :border-top-color color-root-border
   :border-top-width 1
   :flex-direction   :column
   :height           height
   :left             0
   :right            0
   :elevation        2
   :bottom           bottom
   :position         :absolute})

(def header-container
  {:background-color common/color-white
   :alignItems       :center
   :justifyContent   :center})

(def header-title-container
  {:margin-bottom  12
   :flex-direction :row})

(def header-title-text
  {:color        common/color-black
   :flex         1
   :font-size    15
   :text-align   :center
   :padding-top  1
   :margin-left  32
   :margin-right 32})

(def header-close-container
  {:width            24
   :height           24
   :align-items      :center
   :position         :absolute
   :right            6
   :top              -2})

(def header-close-icon
  {:width      12
   :height     12
   :margin-top 6})
