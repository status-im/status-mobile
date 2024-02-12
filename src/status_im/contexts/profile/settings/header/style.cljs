(ns status-im.contexts.profile.settings.header.style
  (:require [quo.foundations.colors :as colors]))

(defn header-view
  [customization-color theme]
  {:background-color (colors/resolve-color customization-color theme 40)
   :min-height       100
   :flex             1})

(def avatar-row-wrapper
  {:display         :flex
   :padding-left    16
   :padding-right   12
   :margin-top      -60
   :margin-bottom   -4
   :align-items     :flex-end
   :justify-content :space-between
   :flex-direction  :row})

(def title-container
  {:padding-horizontal 20
   :padding-vertical   12})

(defn header-middle-shape
  [background-color]
  {:background-color background-color
   :height           48
   :flex-grow        1})

(def radius-container
  {:flex-direction  :row
   :justify-content :space-between})

(defn avatar-container
  [theme]
  {:align-items   :flex-start
   :border-width  4
   :border-color  (colors/theme-colors colors/border-avatar-light colors/neutral-80-opa-80 theme)
   :border-radius 44})
