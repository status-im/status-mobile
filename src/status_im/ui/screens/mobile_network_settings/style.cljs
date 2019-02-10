(ns status-im.ui.screens.mobile-network-settings.style
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as common-styles]))

(def container
  {:flex        1
   :align-items :center})

(def title
  {:height     21
   :margin-top 8})

(def title-text
  (merge
   common-styles/text-title-bold
   {:color colors/black}))

(def details
  {:height        66
   :width         311
   :margin-left   32
   :margin-right  32
   :margin-top    6
   :margin-bottom 10})

(def details-text
  (merge
   common-styles/text-main
   {:color       colors/gray
    :text-align  :center
    :line-height 22}))

(def network-icon
  {:title-color     :blue
   :icon-color      :blue
   :icon-background :blue-light})

(def cancel-icon
  {:title-color     :red
   :icon-color      :red
   :icon-background :red-light})

(def separator
  {:background-color colors/gray-lighter
   :margin-left      72
   :align-self       :stretch
   :height           1
   :margin-top       8})

(def checkbox-line-container
  {:margin-left    71
   :margin-top     13
   :height         29
   :flex-direction :row
   :justify-content :center})

(def checkbox
  {:padding         0
   :justify-content :center
   :align-items     :center
   :width           18
   :height          18
   :border-radius   2
   :margin-top      6})

(def checkbox-icon
  {:tint-color colors/white})

(def checkbox-text-container
  {:justify-content  :center
   :flex             1
   :margin-left      13})

(def checkbox-text
  (merge
   common-styles/text-main
   {:color       colors/black
    :line-height 19}))

(def settings-container
  {:margin-left 69
   :height      44
   :margin-top  6
   :align-items :flex-start})

(def settings-text
  (merge
   common-styles/text-main
   {:color       colors/gray
    :line-height 22}))

(def settings-link
  (merge
   common-styles/text-main
   {:color       colors/blue
    :line-height 22}))
