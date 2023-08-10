(ns quo2.components.wallet.network-bridge.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [network state]
  {:width              136
   :height             44
   :border-width       1
   :border-radius      12
   :padding-vertical   5
   :padding-horizontal 8
   :border-color       (get colors/networks network)
   :opacity            (when (= state :disabled) 0.3)})

(defn add-container
  []
  {:border-style       :dashed
   :border-color       (colors/theme-colors colors/neutral-30 colors/neutral-70)
   :justify-content    :center
   :align-items        :center
   :padding-vertical   0
   :padding-horizontal 0})

(defn loading-skeleton
  [theme]
  {:width            32
   :height           10
   :border-radius    3
   :margin-vertical  4
   :background-color (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)})

(def network-icon
  {:width        12
   :height       12
   :margin-right 4})
