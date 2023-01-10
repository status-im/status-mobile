(ns status-im2.contexts.chat.photo-selector.album-selector.style
  (:require [quo2.foundations.colors :as colors]))

(defn album-container
  [selected?]
  {:flex-direction     :row
   :padding-horizontal 12
   :padding-vertical   8
   :margin-horizontal  8
   :border-radius      12
   :align-items        :center
   :background-color   (when selected? colors/neutral-5)})

(def cover
  {:width         40
   :height        40
   :border-radius 10})

(def divider
  {:padding-horizontal 20
   :margin-top         16
   :margin-bottom      8})
