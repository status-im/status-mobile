(ns status-im2.common.confirmation-drawer.style
  (:require
    [quo.foundations.colors :as colors]))

(defn context-container
  []
  {:flex-direction   :row
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :border-radius    20
   :align-items      :center
   :align-self       :flex-start
   :padding          2
   :margin-top       4
   :margin-bottom    16})

(def buttons-container
  {:flex-direction  :row
   :justify-content :space-between
   :margin-top      25})
