(ns quo.components.separator
  (:require
   [quo.design-system.colors :as colors]
   [quo.react-native :as react]))

(defn separator
  [{:keys [color style]}]
  [react/view
   {:style
    (merge
     style
     {:background-color (colors/get-color (or color :ui-01))
      :align-self       :stretch
      :height           1})}])
