(ns status-im.ui.screens.notifications-center.styles
  (:require [status-im.ui.components.colors :as colors]
            [quo.design-system.colors :as quo-colors]))

(def notification-message-text
  {:flex        1
   :align-self  :stretch
   :line-height 22
   :font-size   15
   :color       (:text-01 @quo-colors/theme)})

(def mention-text
  {:color colors/blue})

(def datetime-text
  {:color          colors/text-gray
   :font-size      10
   :text-align     :right
   :letter-spacing 0.4
   :align-items    :center
   :line-height    12
   :position       :absolute
   :top            17
   :right          16})

(def group-info-container
  {:height          22
   :align-self      :baseline
   :align-items     :center
   :justify-content :center
   :border-radius   11
   :border-color    colors/gray-transparent-40
   :border-width    1
   :margin-top      6
   :margin-bottom   10
   :padding-left    7
   :padding-right   5
   :flex-direction  :row})

(defn notification-container [read]
  {:min-height       64
   :background-color (when-not read colors/blue-light)})

(def notification-content-container
  {:flex 1})

(def photo-container
  {:position :absolute
   :top      12
   :left     16})

(def title-text
  {:margin-left  72
   :margin-top   12
   :margin-right 50})

(def notification-message-container
  {:margin-left  72
   :margin-right 16})

(def group-icon
  {:margin-right 4})

(def community-info-container
  {:flex-direction :row
   :align-items    :center})