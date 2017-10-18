(ns status-im.chat.styles.input.box-header
  (:require [status-im.ui.components.styles :as common]))

(def header-height 33)

(def header-container
  {:background-color common/color-white
   :alignItems       :center
   :justifyContent   :center
   :height           header-height})

(def header-title-container
  {:flex-direction      :row
   :height              header-height
   :border-bottom-color "rgba(193, 199, 203, 0.28)"
   :border-bottom-width 1})

(defn header-title-text [back?]
  {:color        common/color-black
   :flex         1
   :font-size    15
   :text-align   :center
   :padding-top  0
   :margin-left  (if back? 32 72)
   :margin-right 32})

(def header-back-container
  {:width       24
   :height      24
   :margin-left 16
   :top         -4})

(def header-close-container
  {:width        24
   :height       24
   :margin-right 16
   :top          -4})

(def header-icon
  {:width  24
   :height 24})