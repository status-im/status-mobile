(ns status-im.contexts.wallet.sheets.network-preferences.style
  (:require [quo.foundations.colors :as colors]))

(def blur
  {:position      :absolute
   :top           0
   :left          0
   :right         0
   :bottom        0
   :overlay-color colors/neutral-100-opa-70-blur})

(def data-item
  {:margin-horizontal 20
   :margin-vertical   8})
