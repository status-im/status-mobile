(ns status-im2.contexts.chat.lightbox.text-sheet.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))



(def text-style
  {:color             colors/white
   :align-self        :center
   :margin-horizontal 20
   :margin-vertical   12
   :flex-grow 1})


(def bar-container
  {:height          20
   :left            0
   :right           0
   :top             0
   :z-index         1
   :justify-content :center
   :align-items     :center})

(defn bar
  []
  {:width            32
   :height           4
   :border-radius    100
   :background-color colors/white-opa-40
   :border-width 0.5
   :border-color colors/neutral-100})

