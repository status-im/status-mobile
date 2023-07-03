(ns status-im2.contexts.chat.lightbox.text-sheet.style
  (:require [quo2.foundations.colors :as colors]))



(def text-style
  {:color             colors/white
   :align-self        :center
   :margin-horizontal 20
   :margin-vertical   12})


(def bar-container
  {:height          20
   :left            0
   :right           0
   :top             0
   :z-index         1
   :background-color :green
   :justify-content :center
   :align-items     :center})

(defn bar
  []
  {:width            32
   :height           4
   :border-radius    100
   :background-color (colors/theme-colors colors/neutral-100-opa-5 colors/white-opa-10)})
