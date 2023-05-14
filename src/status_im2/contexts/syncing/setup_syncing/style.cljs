(ns status-im2.contexts.syncing.setup-syncing.style
  (:require [quo2.foundations.colors :as colors]))

(defn container-main
  [top]
  {:background-color colors/neutral-95
   :flex             1
   :padding-top      top})

(def page-container
  {:margin-horizontal 20})

(def title-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def navigation-bar
  {:height 56})

(def sync-code
  {:margin-top 36})

(defn qr-container
  [valid-code?]
  (merge {:margin-top       12
          :background-color colors/white-opa-5
          :border-radius    20
          :padding          12}
         (if valid-code?
           {:flex 1}
           {:aspect-ratio 1})))

(def sub-text-container
  {:margin-bottom   8
   :justify-content :space-between
   :align-items     :center
   :flex-direction  :row})

(def valid-cs-container
  {:flex   1
   :margin 12})

(def generate-button
  {:position          :absolute
   :top               "50%"
   :bottom            0
   :left              0
   :right             0
   :margin-horizontal 60})
