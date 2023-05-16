(ns status-im2.contexts.onboarding.identifiers.profile-card.style
  (:require [quo2.foundations.colors :as colors]))

(def card-view
  {:margin-horizontal 20
   :margin-bottom     :auto
   :flex-direction    :row})

(def card-container
  {:padding-horizontal 12
   :padding-top        12
   :padding-bottom     12
   :flex               1
   :border-radius      16})

(def card-header
  {:flex-direction  :row
   :justify-content :space-between})

(def mask-container
  {:position :absolute
   :top      0
   :left     0})

(def mask-view
  {:position         :absolute
   :top              0
   :left             0
   :width            48
   :background-color :transparent
   :height           48
   :border-color     :black
   :border-width     2
   :border-radius    44})

(def picture-avatar-mask
  {:position      :absolute
   :width         48
   :height        48
   :border-radius 48})

(def user-name-container
  {:flex-direction :row
   :margin-top     8
   :align-items    :center
   :padding-right  12})

(def user-name
  {:color colors/white})

(def user-hash
  {:margin-top 2})

(def emoji-hash
  {:margin-top 12})
