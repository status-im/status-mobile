(ns status-im2.contexts.onboarding.notifications.style
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
  {:height          100
   :justify-content :center
   :padding-horizontal 20})

(def enable-notifications-buttons 
  {:margin 20
   :background-color :transparent})

(def blur-screen-container
  {:position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0})


(def illustration
  {:flex               1
   :background-color   colors/danger-50
   :align-items        :center
   :margin-horizontal  20
   :border-radius      20
   :justify-content    :center})

(def notifications-container 
  {:flex             1
   :background-color :transparent})

