(ns status-im2.contexts.onboarding.enter-seed-phrase.style
  (:require [quo2.foundations.colors :as colors]))

(def full-layout {:flex 1})

(def page-container
  {:position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0
   :background-color colors/neutral-80-opa-80-blur})

(def form-container
  {:flex               1
   :padding-horizontal 20
   :padding-vertical   12})

(def header-container
  {:flex-direction  :row
   :justify-content :space-between})

(def word-count-container
  {:justify-content :flex-end
   :margin-bottom   2})

(def input-container
  {:height            120
   :margin-top        12
   :margin-horizontal -20})

(def continue-button {:margin-top :auto})

(def keyboard-container {:margin-top :auto})
