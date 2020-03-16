(ns status-im.ui.screens.multiaccounts.styles
  (:require [status-im.ui.components.colors :as colors]))

(def multiaccounts-view
  {:flex 1})

(def multiaccounts-container
  {:flex               1
   :margin-top         24
   :justify-content    :space-between})

(def bottom-actions-container
  {:margin-bottom 16})

(def multiaccount-image-size 40)

(def multiaccount-title-text
  {:color     colors/black
   :font-size 17})

(def multiaccounts-list-container
  {:flex             1
   :padding-bottom 8})

(def multiaccount-view
  {:flex-direction     :row
   :align-items        :center
   :padding-horizontal 16
   :height             64})

(def multiaccount-badge-text-view
  {:margin-left  16
   :margin-right 31
   :flex-shrink  1})

(def multiaccount-badge-text
  {:font-weight "500"})

(def multiaccount-badge-pub-key-text
  {:font-family "monospace"
   :color       colors/gray})

(def bottom-button
  {:padding-horizontal 24
   :justify-content    :center
   :align-items        :center
   :align-self         :center
   :flex-direction     :row})

(defn bottom-button-container []
  {:flex-direction     :row
   :padding-horizontal 12
   :padding-vertical   8
   :border-top-width   1
   :border-top-color   colors/gray-lighter
   :justify-content    :center
   :align-items        :center})
