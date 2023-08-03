(ns status-im2.contexts.syncing.setup-syncing.style
  (:require [quo2.foundations.colors :as colors]
            [status-im2.contexts.syncing.standard-authentication.view :as standard-auth]))

(def container-main
  {:background-color colors/neutral-95
   :flex             1})

(def page-container
  {:margin-horizontal 20})

(def title-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def navigation-bar
  {:height 56})

(def sync-code
  {:margin-top 20})

(def standard-auth
  {:background-color            colors/white-opa-5
   :border-radius               20
   :border-top-left-radius      0
   :border-top-right-radius     0
   :margin-left                 20
   :margin-top                  0
   :margin-right                32
   :padding                     12
   :padding-top                 2})

(defn qr-container
  [valid-code?]
  (merge {:margin-top                  12
          :background-color            colors/white-opa-5
          :border-radius               20
          :border-bottom-left-radius   0
          :border-bottom-right-radius  0
          :padding                     12}
         (if valid-code?
           {:flex 1  :border-radius 20}
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

(def title-container-22
  {:flex-direction      :row
   :align-items         :center
   :border-radius       20
   :padding-horizontal  20
   :padding-vertical    12
   :margin-top          12
   :justify-content     :space-between
   :background-color    "rgba(42, 74, 245, 0.1)"})