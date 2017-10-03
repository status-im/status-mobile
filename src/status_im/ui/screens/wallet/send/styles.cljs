(ns status-im.ui.screens.wallet.send.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.components.styles :as styles]))

(def toolbar
  {:background-color styles/color-blue5
   :elevation        0
   :padding-bottom   10})

(def toolbar-title-container
  {:flex           1
   :flex-direction :row
   :margin-left    6})

(def toolbar-title-text
  {:color        styles/color-white
   :font-size    17
   :margin-right 4})

(def toolbar-icon
  {:width  24
   :height 24})

(def toolbar-title-icon
  (merge toolbar-icon {:opacity 0.4}))

(defn animated-sign-panel [bottom-value]
  {:position           :absolute
   :left               12
   :right              12
   :bottom             bottom-value})

(defn sign-panel [opacity-value]
  {:opacity            opacity-value
   :border-radius      8
   :background-color   :white
   :padding-top        12
   :padding-horizontal 12})

(def signing-phrase-container
  {:border-radius    8
   :height           36
   :align-items      :center
   :justify-content  :center
   :background-color styles/color-light-gray})

(def signing-phrase
  {:font-size      15
   :letter-spacing -0.2
   :color          :black})

(def signing-phrase-description
  {:padding-top 8})

(def password-container
  {:flex             1
   :padding-vertical 20})

(def password
  {:padding        0
   :font-size      15
   :letter-spacing -0.2
   :height         20})

(def processing-view
  {:position         :absolute
   :top              0
   :bottom           0
   :right            0
   :left             0
   :align-items      :center
   :justify-content  :center
   :background-color (str styles/color-black "1A")})
