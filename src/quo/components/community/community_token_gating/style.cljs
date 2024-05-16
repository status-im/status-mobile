(ns quo.components.community.community-token-gating.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [theme]
  {:padding-bottom  12
   :padding-top     10
   :border-width    1
   :border-color    (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :border-radius   16
   :margin-vertical -4})

(def eligibility-row
  {:flex-direction     :row
   :padding-horizontal 12
   :align-items        :center})

(def divider
  {:height       27
   :padding-left 12
   :padding-top  0
   :align-items  :flex-start})

(def eligibility-label
  {:flex 1})

(def you-hodl
  {:padding-horizontal 12
   :margin-bottom      15})

(def join-button
  {:padding-horizontal 12
   :margin-top         8})

(def token-row
  {:flex-direction     :row
   :padding-horizontal 12
   :row-gap            10
   :column-gap         8
   :flex-wrap          :wrap
   :margin-bottom      11})

;; This wrapper prevents layout shifts caused by border effects on
;; the view's height when the token has a border.
(def token-wrapper
  {:height          26
   :justify-content :center})
