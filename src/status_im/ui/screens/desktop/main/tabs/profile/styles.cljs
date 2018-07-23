(ns status-im.ui.screens.desktop.main.tabs.profile.styles
  (:require [status-im.ui.components.colors :as colors]))

(def profile-view
  {:align-items :center})

(def profile-badge
  {:margin-top    34
   :align-items   :center
   :margin-bottom 16})

(def logout-row
  {:justify-content   :space-between
   :flex-direction    :row
   :margin-horizontal 24
   :align-self        :stretch
   :margin-top        60})

(defn logout-row-text [color]
  {:color     color
   :font-size 16})

(def profile-photo
  {:border-radius 100
   :width         100
   :height        100})

(def profile-user-name
  {:font-weight :bold
   :font-size   18})

(def share-contact-code
  {:flex-direction    :row
   :justify-content   :space-between
   :align-items       :center
   :height            45
   :width             240
   :margin-horizontal 50
   :border-radius     8
   :background-color  (colors/alpha colors/blue 0.1)})

(def share-contact-code-text-container
  {:margin-left     32
   :justify-content :center
   :align-items     :center})

(def share-contact-code-text
  {:color     colors/blue
   :font-size 14})

(def share-contact-icon-container
  {:margin-right    12
   :width           22
   :height          22
   :align-items     :center
   :justify-content :center})

(def qr-code-container
  {:align-items    :center
   :padding-top    16
   :padding-bottom 46
   :padding-left   58
   :padding-right  58})

(def close-icon-container
  {:flex            1
   :margin-top      22
   :margin-right    22
   :margin-bottom   16
   :flex-direction  :row
   :justify-content :flex-end})

(def close-icon
  {:height       24
   :width        24
   :tint-color   colors/gray-icon})

(def check-icon
  {:height       16
   :width        16
   :margin-right 8
   :tint-color   colors/tooltip-green-text})

(def qr-code-title
  {:font-size     20
   :font-weight   "600"
   :margin-bottom 32})

(def qr-code
  {:width         130
   :height        130
   :margin-bottom 16})

(def qr-code-text
  {:font-size     16
   :text-align    :center
   :margin-bottom 16})

(def qr-code-copy
  {:width            185
   :height           45
   :border-radius    8
   :background-color colors/blue
   :justify-content  :center
   :align-items      :center})

(def qr-code-copy-text
  {:font-size 16
   :color     colors/white})

(defn tooltip-container [opacity]
  {:position    :absolute
   :align-items :center
   :opacity     opacity
   :top         -34})

(def tooltip-icon-text
  {:flex-direction   :row
   :justify-content  :space-between
   :align-items      :center
   :height           24
   :border-radius    8
   :padding-left     10
   :padding-right    10
   :background-color colors/tooltip-green})

(def tooltip-triangle
  {:width              0
   :height             0
   :border-top-width   9.1
   :border-left-width  9.1
   :border-right-width 9.1
   :border-left-color  :transparent
   :border-right-color :transparent
   :border-top-color   colors/tooltip-green})
