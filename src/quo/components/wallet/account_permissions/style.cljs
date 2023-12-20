(ns quo.components.wallet.account-permissions.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [theme]
  {:font-size     30
   :border-radius 16
   :border-width  1
   :border-color  (colors/theme-colors colors/neutral-10 colors/neutral-90 theme)})

(def row1
  {:flex-direction     :row
   :height             56
   :padding-horizontal 12
   :align-items        :center})

(def account-details
  {:flex              1
   :margin-horizontal 8})

(def name-and-keycard
  {:flex-direction :row
   :align-items    :center
   :gap            4})

(def row2-content
  {:flex-direction    :row
   :flex-wrap         :wrap
   :margin-vertical   4
   :margin-horizontal 8})

(def no-relevant-tokens
  {:color  colors/neutral-40
   :margin 4})

(def token-and-text
  {:margin 4})
