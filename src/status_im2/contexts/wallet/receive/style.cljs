(ns status-im2.contexts.wallet.receive.style
  (:require [quo.foundations.colors :as colors]))

(def blur
  {:position      :absolute
   :top           0
   :left          0
   :right         0
   :bottom        0
   :overlay-color colors/neutral-80-opa-80-blur})

(def header-container
  {:padding-horizontal 20
   :padding-vertical   12})
