(ns status-im.contexts.profile.contact.header.style
  (:require [quo.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(def avatar-wrapper
  {:margin-top   -40
   :padding-left 20
   :align-items  :flex-start})

(def button-wrapper
  {:margin-top         8
   :margin-bottom      16
   :padding-horizontal 20})

(def username-wrapper
  {:margin-top         12
   :padding-horizontal 20})

(defn header-container
  [border-radius theme margin-top]
  (reanimated/apply-animations-to-style
   {:border-top-left-radius  border-radius
    :border-top-right-radius border-radius}
   {:background-color   (colors/theme-colors colors/white colors/neutral-95 theme)
    :padding-horizontal 20
    :margin-top         margin-top}))

(def status-tag-wrapper
  {:flex-direction :row
   :padding-top    12
   :padding-right  12})

(def header-top-wrapper
  {:flex-direction  :row
   :justify-content :space-between})
