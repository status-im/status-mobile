(ns status-im2.contexts.shell.share.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(def screen-padding 20)

(def blur
  {:position      :absolute
   :top           0
   :left          0
   :right         0
   :bottom        0
   :overlay-color colors/neutral-80-opa-80-blur})

(def header-row
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal screen-padding
   :margin-vertical    12})

(def header-button
  {:margin-bottom    12
   :background-color colors/white-opa-5})

(def header-heading
  {:padding-horizontal screen-padding
   :padding-vertical   12
   :color              colors/white})

(def qr-code-container
  {:padding-top        12
   :padding-bottom     8
   :padding-horizontal 12
   :border-radius      16
   :margin-top         8
   :margin-horizontal  screen-padding
   :background-color   colors/white-opa-5
   :flex-direction     :column
   :justify-content    :center})

(def emoji-hash-container
  {:border-radius     16
   :margin-top        12
   :margin-horizontal screen-padding
   :background-color  colors/white-opa-5
   :flex-direction    :row
   :justify-content   :flex-start
   :align-items       :flex-start})

(def profile-address-column
  {:flex-direction :column})

(def emoji-address-column
  {:flex-direction :column})

(def profile-address-label
  {:color       colors/white-opa-40
   :padding-top 10})

(def profile-address-content
  {:color       colors/white
   :align-self  :flex-start
   :padding-top 2})

(def profile-address-container
  {:flex-direction  :row
   :justify-content :flex-start
   :margin-top      4})

(def emoji-address-container
  {:flex-direction  :row
   :justify-content :flex-start})

(def emoji-hash-label
  {:color          colors/white-opa-40
   :margin-top     8
   :padding-bottom (if platform/ios? 2 0)
   :padding-left   12})


(def share-button-container
  {:position :absolute
   :right    0
   :top      12})

(def emoji-share-button-container
  {:position :absolute
   :right    0
   :top      12})

(def emoji-hash-content
  {:color          colors/white
   :align-self     :flex-start
   :padding-top    4
   :padding-bottom 12
   :padding-left   12
   :font-size      13})

(def tabs-container
  {:padding-horizontal screen-padding
   :margin-vertical    8})

(def wip-style
  {:color      colors/white
   :text-align :center})
