(ns quo.components.wallet.transaction-progress.style
  (:require [quo.foundations.colors :as colors]))

(def title-text-container
  {:flex 1})

(def icon
  {:margin-right 4})

(defn box-style
  [theme]
  {:border-radius 16
   :border-width  1
   :border-color  (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})

(def title-container
  {:align-items        :center
   :flex-direction     :row
   :padding-left       12
   :padding-top        8
   :padding-right      8
   :padding-bottom     4})

(defn progress-box-container
  [bottom-large?]
  {:flex-direction :row
   :align-items    :center
   :padding-horizontal   12 
   :padding-bottom (if bottom-large? 12 8)
   :padding-top 4
   :flex-wrap      :wrap})

(def status-row-container
  {:flex-direction     :row
   :align-items        :center
   :flex 1
   :padding-horizontal 12 
   :padding-top        8
   :padding-bottom     4})

(defn context-tag-container
  [theme]
  {:padding-horizontal 12
   :padding-bottom     8
   :flex-direction    :row
   :border-bottom-width 1
   :border-color       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})
