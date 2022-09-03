(ns quo2.components.separator
  (:require [quo.react-native :as react]
            [quo2.foundations.colors :as quo2.colors]))

(defn separator [{:keys [style]}]
  [react/view
   {:style
    (merge
     style
     {:background-color (quo2.colors/theme-colors
                         quo2.colors/neutral-30
                         quo2.colors/neutral-80)
      :align-self       :stretch
      :height           1})}])