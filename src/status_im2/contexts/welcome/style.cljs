(ns status-im2.contexts.welcome.style
  (:require
    [quo2.foundations.colors :as colors]))

(def title-text-style
  {:accessibility-label :notifications-screen-title
   :weight              :semi-bold
   :size                :heading-1})

(def subtitle-text-style
  {:accessibility-label :notifications-screen-sub-title
   :weight              :regular
   :size                :paragraph-1})

(def title-container
  {:justify-content    :center
   :margin-top         12
   :padding-horizontal 20})

(def welcome-buttons
  {:margin           20
   :background-color :transparent})

(def blur-screen-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def illustration
  {:flex              1
   :background-color  colors/danger-50
   :align-items       :center
   :margin-horizontal 20
   :border-radius     20
   :margin-top        20
   :justify-content   :center})

(def welcome-container
  {:flex             1
   :background-color :transparent})

