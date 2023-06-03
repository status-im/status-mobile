(ns status-im2.contexts.chat.messages.drawers.style
  (:require [quo2.foundations.colors :as colors]))

(def tab
  {:flex-direction  :row
   :align-items     :center
   :justify-content :center})

(def tab-icon {:margin-right 4
               :width 20
               :height 20})

(defn tab-count
  [active?]
  {:color (if (or active? (colors/dark?)) colors/white colors/neutral-100)})

(def tabs-container
  {:flex          1
   :align-self    :stretch
   :padding-left  20
   :padding-right 8
   :margin-bottom 12})

(def authors-list
  {:height 320
   :flex   1})
