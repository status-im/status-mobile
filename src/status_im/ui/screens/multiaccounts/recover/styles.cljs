(ns status-im.ui.screens.multiaccounts.recover.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]))

(def window-width (:width (react/get-dimensions "window")))
(def window-height (:height (react/get-dimensions "window")))
(defn scaled-x [n] (* (/ window-width 375) n))
(defn scaled-y [n] (* (/ window-height 812) n))

(def screen-container
  {:flex 1})

(def password-input
  {:margin-top 10})

(def bottom-button-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})

(def processing-view
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def sign-you-in
  {:margin-top     16
   :font-size      13})

(def recover-release-warning
  {:margin-top     16
   :font-size      12
   :color          colors/gray})

(defn recovery-phrase-input [processing?]
  {:position :absolute
   :left "6.4%"
   :right "6.4%"
   :top "29.06%"
   :bottom "65.52%"
   :font-weight (if processing? "400" "700")
   :font-size 16
   :line-height 22
   :text-align :center
   :color          colors/black})

(def top-line-container
  {:margin  16
   :desktop {:padding-top 15}})

(def recovery-cancel
  {:position :absolute
   :width        "10.4%" ; 39/375
   :height       "2.71%" ; 22/812
   :left    "5.6%" ; 21/375 
   :top     "7.64%" ; 62/812
   :font-size      15
   :line-height    22
   :text-align     :center
   :color          colors/blue})

(def step-n
  {:position :absolute
   :left "8.53%"
   :right "8.53%"
   :top "7.76%"
   :bottom "89.53%"
   :font-size 15
   :line-height 22
   :text-align :center
   :color "#939BA1"})

(def recovery-phrase-title
  {:position :absolute
   :left "4.27%"
   :right "4.27%"
   :top "14.29%"
   :bottom "82.27%"
   :font-size 22
   :line-height 28
   :text-align :center
   :color      colors/black})

(def recovery-phrase-explainer
  {:position :absolute
   :left "6.4%"
   :right "6.13%"
   :top "19.7%"
   :bottom "74.88%"
   :font-size 15
   :line-height 22
   :text-align :center
   :color "#939BA1"})

(def key-recovered-title
  {:position :absolute
   :left "6.93%"
   :right "6.93%"
   :top "14.29%"
   :bottom "78.82%"
   :font-size 22
   :line-height 28
   :text-align :center
   :color "#000000"})

(def key-recovered-explainer
  {:position :absolute
   :left "6.4%"
   :right "6.13%"
   :top "23.03%"
   :bottom "71.55%"
   :font-size 15
   :line-height 22
   :text-align :center
   :color "#939BA1"})

(def key-recovered-image-container
  {:position :absolute
   :width "100%"
   :left 0
   :top "42.7%" ; 347/812
   :align-items     :center
   :justify-content :center})

(def key-recovered-image-circle
  {:width 61
   :height 61
   :border-width 1
   :border-radius 30.5
   :border-color      colors/black
   :align-items     :center
   :justify-content :center})

(def key-recovered-multiaccount-name
  {:position :absolute
   :width "47.2%" ; 177/375
   :height "2.71%" ; 22/812
   :left "26.4%" ; 99/375
   :top "51.6%" ; 419/812
   :font-weight "500"
   :font-size 15
   :line-height 22
   :flex 1
   :align-items :center
   :text-align :center
   :color "#000000"})

(def key-recovered-multiaccount-address
  {:position :absolute
   :left "11.7%" ; 44/375
   :right "11.7%" ; 44/375
   :top "54.8%" ; 445/812
   :bottom "42.7%" ; 347/812
   :font-family "Roboto Mono"
   :font-size 15
   :line-height 22
   :flex 1
   :align-items :center
   :text-align :center
   :color "#939BA1"})

(def key-recovered-reencrypt-your-key-button
  {:position :absolute
   :height "5.42%" ; 44/812
   :left "24.3%" ; 91/375
   :right "24.3%" ; 91/375
   :top "87.4%" ; 710/812
   :align-items :center
   :text-align :center})

(def recovery-error-message
  {:position :absolute
   :left "6.4%"
   :right "6.13%"
   :top "36.45%"
   :bottom "58.13%"
   :font-size 15
   :line-height 22
   :color "#FF2D55"
   :align-items :center
   :text-align :center})
