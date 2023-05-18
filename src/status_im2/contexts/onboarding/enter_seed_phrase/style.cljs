(ns status-im2.contexts.onboarding.enter-seed-phrase.style
  (:require [quo2.foundations.colors :as colors]))

(def page-container
  {:position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0
   :background-color colors/neutral-80-opa-80-blur})

(def input-container
  {:height            120
   :margin-horizontal -20})
