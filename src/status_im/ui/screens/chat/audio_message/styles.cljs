(ns status-im.ui.screens.chat.audio-message.styles
  (:require [status-im.ui.components.colors :as colors]))

(def container
  {:flex            1
   :flex-direction  :column
   :justify-content :space-around})

(def timer
  {:font-size 28
   :line-height 38
   :align-self :center})

(def buttons-container
  {:flex            1
   :max-height 80
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-around
   :align-self :stretch
   :padding-horizontal 80})

(def rec-button-base-size 61)

(def rec-button-container
  {:width       rec-button-base-size
   :height      rec-button-base-size
   :align-items "center"})

(defn rec-outer-circle [scale-anim]
  {:position      "absolute"
   :width         rec-button-base-size
   :height        rec-button-base-size
   :top           0
   :border-width  4
   :transform     [{:scale scale-anim}]
   :border-color  colors/red-audio-recorder
   :border-radius rec-button-base-size})

(defn rec-inner-circle [scale-anim border-radius-anim]
  {:position         "absolute"
   :top              6
   :left             6
   :bottom           6
   :right            6
   :transform        [{:scale scale-anim}]
   :border-radius    border-radius-anim
   :background-color colors/red-audio-recorder})