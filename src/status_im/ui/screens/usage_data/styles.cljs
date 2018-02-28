(ns status-im.ui.screens.usage-data.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def usage-data-view
  {:flex               1
   :padding-horizontal 30
   :background-color   colors/white})

(def logo-container
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def logo
  {:size      82
   :icon-size 34})

(def usage-data-image
  {:width      138
   :height     208
   :margin-top 10})

(defstyle help-improve-text
  {:text-align :center
   :color      colors/black
   :ios        {:line-height    28
                :font-size      22
                :font-weight    :bold
                :letter-spacing -0.3}
   :android    {:font-size   24
                :line-height 30}})

(def help-improve-text-description
  {:line-height    21
   :margin-top     8
   :margin-bottom  16
   :font-size      14
   :letter-spacing -0.2
   :text-align     :center
   :color          colors/gray})

(def buttons-container
  {:align-items :center})

(def bottom-button-container
  {:margin-bottom 6
   :margin-top    38})