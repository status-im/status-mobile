(ns quo.components.separator
  (:require [quo.react-native :as react]
            [quo.design-system.colors :as colors]
            [quo2.foundations.colors :as quo2.colors]))

(defn separator [{:keys [color style]}]
  [react/view
   {:style
    (merge
     style
     {:background-color (colors/get-color (or color :ui-01))
      :align-self       :stretch
      :height           1})}])

(defn separator-redesign [{:keys [style]}]
  [react/view
   {:style
    (merge
     style
     {:background-color (quo2.colors/theme-colors
                         quo2.colors/neutral-30
                         quo2.colors/neutral-80)
      :align-self       :stretch
      :height           1})}])
