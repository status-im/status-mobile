(ns status-im.contexts.wallet.send.input-amount.style
  (:require [quo.foundations.colors :as colors]))

(def screen
  {:flex 1})

(def input-container
  {:padding-top    12
   :padding-bottom 0})

(defn keyboard-container
  [bottom]
  {:padding-bottom bottom})

(def estimated-fees-container
  {:height             48
   :width              "100%"
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 20
   :padding-top        8})

(def estimated-fees-content-container
  {:align-items :center
   :height      40})

(def fees-data-item
  {:flex             1
   :height           40
   :background-color :transparent})

(def amount-data-item
  {:flex             1
   :height           40
   :background-color :transparent})

(def no-routes-found-container
  {:height      40
   :width       "100%"
   :align-items :center})

(defn token-not-available-container
  [theme]
  {:height            90
   :flex-direction    :row
   :background-color  (colors/resolve-color :danger theme 5)
   :border-color      (colors/resolve-color :danger theme 10)
   :border-width      1
   :border-radius     12
   :margin-horizontal 20
   :padding           12})

(def token-not-available-content-container
  {:margin-left 8
   :align-items :flex-start})

(defn token-not-available-text
  [theme]
  {:height 36
   :flex   1
   :color  (colors/resolve-color :danger theme)})
