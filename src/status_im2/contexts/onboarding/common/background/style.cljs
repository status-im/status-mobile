(ns status-im2.contexts.onboarding.common.background.style
  (:require [quo2.foundations.colors :as colors]))

(def background-container
  {:background-color colors/neutral-95
   :flex-direction   :row
   :position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0})

(defn background-gradient-overlay
  [dark-overlay?]
  {:position :absolute
   :height   (if dark-overlay? 240 136)
   :top      0
   :left     0
   :right    0
   :bottom   0})

(def background-blur-overlay
  {:position         :absolute
   :left             0
   :top              0
   :bottom           0
   :right            0
   :background-color colors/neutral-80-opa-80-blur})
