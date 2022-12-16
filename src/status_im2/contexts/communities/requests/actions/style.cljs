(ns status-im2.contexts.communities.requests.actions.style
  (:require [quo2.foundations.colors :as colors]))

(def community-rule
  {:height           18
   :width            18
   :margin-left      1
   :margin-right     9
   :background-color colors/white
   :border-color     colors/neutral-20
   :border-width     1
   :border-radius    6})

(def community-rule-text
  {:margin-left   :auto
   :margin-right  :auto
   :margin-top    :auto
   :margin-bottom :auto})

(def request-container1
  {:flex          1
   :margin-left   20
   :margin-right  20
   :margin-bottom 20})

(def request-container2
  {:flex            1
   :flex-direction
   :row
   :align-items     :center
   :justify-content :space-between})

(def request-icon
  {:height           32
   :width            32
   :align-items      :center
   :background-color colors/white
   :border-color     colors/neutral-20
   :border-width     1
   :border-radius    8
   :display          :flex
   :justify-content  :center})

(def request-button
  {:margin-top      32
   :margin-bottom   16
   :flex            1
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-evenly})
