(ns status-im.components.button.styles
  (:require [status-im.components.styles :as st]))

(def border-color st/color-white-transparent-2)

(def button-borders
  {:background-color border-color
   :margin           5
   :border-radius    8})

(def action-buttons-container
  (merge
    button-borders
    {:flex-direction :row}))

(def action-button
  {:flex-basis         0
   :flex               1
   :align-items        :center})

(def action-button-center
  (merge action-button
         {:border-color       border-color
          :border-left-width  1
          :border-right-width 1}))

(def action-button-text
  {:font-size          18
   :font-weight        "normal"
   :color              st/color-white
   :padding-horizontal 16
   :padding-vertical   9})

(def primary-button
  (merge
    action-button
    button-borders
    {:background-color st/color-blue4}))

(def primary-button-text
  (merge
    action-button-text
    {:color st/color-white}))

(def secondary-button
  (merge
    action-button
    button-borders
    {:background-color st/color-blue4-transparent}))

(def secondary-button-text
  (merge
    action-button-text
    {:color   st/color-blue4}))

(def action-button-text-disabled
  (merge action-button-text {:opacity 0.4}))