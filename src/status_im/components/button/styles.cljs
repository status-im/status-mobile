(ns status-im.components.button.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.components.styles :as styles]))

(def border-color styles/color-white-transparent-2)

(defstyle button-borders
  {:background-color  border-color
   :android           {:border-radius 4}
   :ios               {:border-radius 8}})

(def action-buttons-container
  (merge
    button-borders
    {:flex-direction :row}))

(def button-container styles/flex)

(def action-button
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center})

(def action-button-center
  (merge action-button
         {:border-color       border-color
          :border-left-width  1
          :border-right-width 1}))

(defstyle action-button-text
  {:font-size          15
   :font-weight        "normal"
   :color              styles/color-white
   :padding-horizontal 16
   :android            {:padding-vertical   10
                        :letter-spacing     0.2}
   :ios                {:padding-vertical   9}})

(def primary-button
  (merge
    action-button
    button-borders
    {:background-color styles/color-blue4}))

(def primary-button-text
  (merge
    action-button-text
    {:color styles/color-white}))

(def secondary-button
  (merge
    action-button
    button-borders
    {:background-color styles/color-blue4-transparent}))

(def secondary-button-text
  (merge
    action-button-text
    {:color   styles/color-blue4}))

(def action-button-text-disabled {:opacity 0.4})