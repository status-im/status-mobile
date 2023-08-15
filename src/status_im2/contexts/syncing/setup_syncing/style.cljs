(ns status-im2.contexts.syncing.setup-syncing.style
  (:require [quo2.foundations.colors :as colors]))

(def container-main
  {:background-color colors/neutral-95
   :flex             1})

(def page-container
  {:margin-top 14
   :margin-horizontal 20})

(def title-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def navigation-bar
  {:height 56})

(def sync-code
  {:margin-top 20})

(def standard-auth
  {:margin-top 12
   :flex 1

   })

(def qr-container
  {:margin-top                 12
   :background-color           colors/white-opa-5
   :border-radius              20
   :flex 1
   :padding                    12})

(def sub-text-container
  {:margin-bottom   8
   :justify-content :space-between
   :align-items     :center
   :flex-direction  :row})

(def valid-cs-container
  {:flex   1
   :margin 12})

