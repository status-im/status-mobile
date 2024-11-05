(ns quo.components.buttons.wallet-ctas.style
  (:require [quo.foundations.colors :as colors]))

(def inner-container
  {:flex-direction  :row
   :justify-content :space-between
   :flex            1})

(def button-container
  {:padding-vertical 8
   :flex             1
   :justify-content  :center
   :align-items      :center})

(defn action-button-text
  [theme disabled?]
  (cond-> {:margin-top 4
           :color      (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}
    disabled? (assoc :opacity 0.5)))
