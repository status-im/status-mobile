(ns status-im2.contexts.share.style
  (:require [quo2.foundations.colors :as colors]))

(def screen-padding 20)

(def header-button
  {:margin-bottom 12
   :margin-left   screen-padding})

(def header-heading
  {:padding-horizontal screen-padding
   :padding-vertical   12
   :color              colors/white})

(defn screen-container
  [window-width top bottom]
  {:flex           1
   :width          window-width
   :padding-top    (if (pos? top) (+ top 12) 12)
   :padding-bottom bottom})

(def tabs
  {:padding-left screen-padding})

(def qr-code-container
  {:padding           12
   :border-radius     16
   :margin-top        12
   :margin-bottom     4
   :margin-horizontal screen-padding
   :background-color  colors/white-opa-5
   :flex-direction    :column
   :justify-content   :center
   :align-items       :center})


(def emoji-hash-container
  {:border-radius     16
   :margin-top        12
   :margin-horizontal screen-padding
   :background-color  colors/white-opa-5
   :flex-direction    :row
   :justify-content   :space-between
   :align-items       :center})

(def profile-address-column
  {:flex-direction :column})

(def profile-address-label
  {:align-self  :flex-start
   :color       colors/white-opa-40
   :padding-top 10})

(defn profile-address-content
  [max-width]
  {:color       colors/white
   :align-self  :flex-start
   :padding-top 2
   :max-width   max-width})

(defn profile-address-container
  [qr-size]
  {:flex-direction  :row
   :justify-content :space-between
   :margin-top      6
   :width           qr-size})

(def emoji-hash-label
  {:color        colors/white-opa-40
   :padding-left 12
   :margin-top   8})

(def share-button-container
  {:flex-direction  :column
   :justify-content :center
   :align-items     :center
   :padding-left    12})

(defn emoji-hash-content
  [max-width]
  {:color          colors/white
   :align-self     :flex-start
   :padding-top    4
   :padding-bottom 8
   :padding-left   12
   :font-size      14
   :max-width      max-width})

(def emoji-share-button-container
  {:padding          8
   :position         :absolute
   :background-color colors/white-opa-5
   :border-radius    10
   :right            14
   :top              2})

(def tabs-container
  {:padding-horizontal screen-padding
   :margin-vertical    8})
