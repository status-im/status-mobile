(ns status-im.ui.components.image-button.styles
  (:require [status-im.ui.components.styles :as styles]))

(def image-button
  {:position    :absolute
   :bottom      2
   :right       16
   :flex        1
   :height      50
   :align-items :center})

(def image-button-content
  {:flex           1
   :flex-direction :row
   :height         50
   :align-items    :center
   :align-self     :center})

(def image-button-text
  {:flex           1
   :flex-direction :column
   :letter-spacing -0.3
   :margin-left    8})

(def scan-button-text
  (merge image-button-text {:color styles/color-blue}))

(def show-qr-button-text
  (merge image-button-text {:color "#838c93"}))
