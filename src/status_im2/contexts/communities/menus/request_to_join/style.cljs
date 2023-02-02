(ns status-im2.contexts.communities.menus.request-to-join.style
  (:require [quo2.foundations.colors :as colors]))

(def community-rule
  {:height           18
   :width            18
   :margin-left      1
   :margin-right     9
   :background-color colors/white
   :border-color     colors/neutral-20
   :border-width     1
   :border-radius    6})

(def community-rule-container
  {:flex       1
   :margin-top 16})

(def inner-community-rule-container
  {:flex           1
   :flex-direction :row
   :align-items    :center})

(def community-rule-text
  {:margin-left   :auto
   :margin-right  :auto
   :margin-top    :auto
   :margin-bottom :auto})

(def community-rule-sub-text
  {:margin-left 28
   :margin-top  1})

(def page-container
  {:margin-left   20
   :margin-right  20
   :margin-bottom 20})

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

(defn bottom-container [safe-area]
  {:padding-top     32
   :padding-bottom  (:bottom safe-area)
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-evenly})
