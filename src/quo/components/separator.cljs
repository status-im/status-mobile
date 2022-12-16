(ns quo.components.separator
  (:require [quo.react-native :as react]
            [quo.design-system.colors :as colors]))

(defn separator [{:keys [color style]}]
  [react/view
   {:style
    (merge
     style
     {:background-color (colors/get-color (or color :ui-01))
      :align-self       :stretch
      :height           1})}])
