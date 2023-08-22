(ns status-im2.contexts.wallet.common.tabs.style
  (:require [quo2.foundations.colors :as colors]))

(def empty-container
  {:justify-content :center
   :align-items     :center
   :margin-bottom   44
   :flex            1})

(def image-placeholder
  {:width            80
   :height           80
   :background-color colors/danger})

