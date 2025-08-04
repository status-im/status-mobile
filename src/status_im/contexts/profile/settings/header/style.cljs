(ns status-im.contexts.profile.settings.header.style
  (:require [quo.foundations.colors :as colors]
            [react-native.platform :as platform]))

(def avatar-row-wrapper
  {:padding-left    20
   :padding-right   12
   :margin-top      -65
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

(defn avatar-border-color
  [theme]
  (if platform/android?
    colors/neutral-80-opa-80 ;; Fix is not needed because Android doesn't use blur
    (colors/theme-colors colors/border-avatar-light colors/neutral-80-opa-80 theme)))
