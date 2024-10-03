(ns quo.components.buttons.wallet-ctas.style
  (:require [quo.foundations.colors :as colors]))

(def inner-container
  {:flex-direction  :row
   :justify-content :center
   :flex            1})

(def button-container
  {:padding-vertical 8
   :width            77.75
   :justify-content  :center
   :align-items      :center})

(defn action-button-text
  [theme disabled?]
  (cond-> {:margin-top 4
           :color      (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}
    disabled? (assoc :opacity 0.5)))
