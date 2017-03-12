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
   :justifyContent   :center
   :height           35})

(def header-title-container
  {:margin-bottom  15
   :flex-direction :row})

(def header-title-text
  {:color        common/color-black
   :flex         1
   :font-size    15
   :text-align   :center
   :padding-top  1
   :margin-left  72
   :margin-right 32})

(def header-close-container
  {:width        24
   :height       24
   :margin-right 16
   :top -3})

(def header-close-icon
  {:width      24
   :height     24})
