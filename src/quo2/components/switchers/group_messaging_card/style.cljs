(ns quo2.components.switchers.group-messaging-card.style
  (:require
    [quo2.foundations.colors :as colors]))

(def avatar-container
  {:left          10
   :top           -30
   :border-radius 48
   :border-width  2
   :border-color  colors/neutral-95
   :position      :absolute})

(def content-container
  {:flex            1
   :flex-direction  :column
   :justify-content :space-between
   :margin-top      16})
