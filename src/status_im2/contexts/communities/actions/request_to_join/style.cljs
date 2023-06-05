(ns status-im2.contexts.communities.actions.request-to-join.style
  (:require [quo2.foundations.colors :as colors]))

(def page-container
  {:margin-horizontal 20
   :margin-bottom     20})

(def title-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def request-icon
  {:height           32
   :width            32
   :align-items      :center
   :background-color colors/white
   :border-color     colors/neutral-20
   :border-width     1
   :border-radius    8
   :display          :flex
   :justify-content  :center})

(def cancel-button
  {:flex         1
   :margin-right 12})

(defn bottom-container
  []
  {:margin-horizontal 20
   :padding-top       32
   :flex-direction    :row
   :align-items       :center
   :justify-content   :space-evenly})

(def final-disclaimer-container
  {:margin-top         12
   :padding-horizontal 40})

(def final-disclaimer-text
  {:color      colors/neutral-50
   :text-align :center})
