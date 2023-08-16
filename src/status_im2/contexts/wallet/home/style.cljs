(ns status-im2.contexts.wallet.home.style
  (:require [quo2.foundations.colors :as colors]))

(def tabs
  {:padding-horizontal 20
   :padding-top        8
   :padding-bottom     12})

(def empty-container
  {:justify-content :center
   :align-items     :center
   :margin-bottom   44
   :flex            1})

(def image-placeholder
  {:width            80
   :height           80
   :background-color colors/danger})

(def accounts-list
  {:padding-horizontal 20
   :padding-top        32
   :padding-bottom     12
   :max-height         112})
