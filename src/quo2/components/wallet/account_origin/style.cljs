(ns quo2.components.wallet.account-origin.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [theme]
  {:border-radius 16
   :border-width  1
   :border-color  (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})

(defn title
  [color]
  {:margin-horizontal 12
   :margin-top        8
   :margin-bottom     4
   :color             color})

(defn row-container
  [type theme]
  {:flex-direction     :row
   :padding-horizontal 12
   :padding-top        8
   :margin             (when (= :derivation-path type) 8)
   :border-radius      12
   :border-width       (when (= :derivation-path type) 1)
   :border-color       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
   :padding-bottom     (if (not= :private-key type) 8 12)})

(def icon-container
  {:margin-right 8})

(defn stored-title
  [theme]
  {:color        (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :margin-right 4})

(def row-subtitle-container
  {:flex-direction :row
   :align-items    :center})

(def right-icon-container
  {:justify-content :center})

(def row-content-container
  {:flex 1})
