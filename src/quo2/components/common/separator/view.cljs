(ns quo2.components.common.separator.view
  (:require [quo2.foundations.colors :as quo2.colors]
            [react-native.core :as rn]))

(defn separator
  [{:keys [style]}]
  [rn/view
   {:style
    (merge
     style
     {:background-color (quo2.colors/theme-colors
                         quo2.colors/neutral-10
                         quo2.colors/neutral-80)
      :align-self       :stretch
      :height           1})}])
