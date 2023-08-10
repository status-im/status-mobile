(ns quo2.components.wallet.transaction-progress.style
  (:require [quo2.foundations.colors :as colors]))

(def title-container
  {:flex 1})

(defn title
  [override-theme]
  {:line-height 18.2})

(def icon
  {:margin-right 6})

(def box-style
  {:border-radius 16
   :border-width  1
   :border-color  colors/neutral-10})

(def title-item-container
  {:align-items        :center
   :flex-direction     :row
   :padding-horizontal 12
   :padding-top        8
   :padding-bottom     5})

(def item-container
  {:align-items    :center
   :flex-direction :row
   :padding-top    7})

(def progress-box-container
  {:align-items        :center
   :padding-horizontal 12
   :flex-direction     :row
   :padding-bottom     11
   :flex-wrap          "wrap"})

(def progress-container
  {:flex-direction     :row
   :border-top-width   1
   :padding-horizontal 12
   :border-color       colors/neutral-5
   :padding-top        8
   :padding-bottom     4})

(def inner-container
  {:flex-direction :row})

(def padding-row
  {:padding-horizontal 12
   :flex-direction     :row})

(def progress-box
  {:width             8
   :height            12
   :border-width      1
   :border-radius     3
   :border-color      colors/neutral-80-opa-5
   :background-color  colors/neutral-5
   :margin-horizontal 2
   :margin-vertical   2})

(def progress-box-arbitrum
  {:width             "95%"
   :height            12
   :border-width      1
   :border-radius     3
   :margin-horizontal 0
   :margin-vertical   2})

(def progress-box-arbitrum-abs
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})
