(ns status-im.ui.screens.intro.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def intro-view
  {:flex               1
   :padding-horizontal 30
   :background-color   colors/white})

(def intro-logo-container
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def intro-logo
  {:size      111
   :icon-size 46})

(defstyle intro-text
  {:text-align :center
   :color      colors/black
   :ios        {:line-height    28
                :font-size      22
                :font-weight    :bold
                :letter-spacing -0.3}
   :android    {:font-size   24
                :line-height 30}})

(def intro-text-description
  {:line-height    21
   :margin-top     8
   :margin-bottom  16
   :text-align     :center
   :color          colors/gray})

(def buttons-container
  {:align-items :center})

(def bottom-button-container
  {:margin-bottom 6
   :margin-top    38})
