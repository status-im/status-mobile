(ns status-im2.contexts.shell.scan.style
  (:require [quo2.foundations.colors :as colors]))

(def screen-padding 20)

(def header-container
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal screen-padding
   :margin-vertical    12})

(def header-text
  {:padding-horizontal screen-padding
   :padding-top        12
   :padding-bottom     8
   :color              colors/white})

(def blur
  {:style         {:position :absolute
                   :top      0
                   :left     0
                   :right    0
                   :bottom   0}
   :overlay-color colors/neutral-80-opa-80
   :blur-amount   20})