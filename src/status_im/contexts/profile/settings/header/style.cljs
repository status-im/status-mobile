(ns status-im.contexts.profile.settings.header.style
  (:require [quo.foundations.colors :as colors]))

(def avatar-row-wrapper
  {:display         :flex
   :padding-left    20
   :padding-right   12
   :margin-top      -60
   :margin-bottom   -4
   :align-items     :flex-end
   :justify-content :space-between
   :flex-direction  :row})

(defn header-middle-shape
  [background-color]
  {:background-color background-color
   :height           48
   :flex-grow        1})

(defn radius-container
  [opacity-animation]
  {:opacity         opacity-animation
   :flex-direction  :row
   :justify-content :space-between})

(defn avatar-container
  [theme scale-animation top-margin-animation side-margin-animation]
  [{:transform     [{:scale scale-animation}]
    :margin-top    top-margin-animation
    :margin-left   side-margin-animation
    :margin-bottom side-margin-animation}
   {:align-items   :flex-start
    :border-width  4
    :border-color  (colors/theme-colors colors/border-avatar-light colors/neutral-80-opa-80 theme)
    :border-radius 100}])
