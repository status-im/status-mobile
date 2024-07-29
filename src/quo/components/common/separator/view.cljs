(ns quo.components.common.separator.view
  (:require
    [quo.foundations.colors :as quo.colors]
    [quo.theme]
    [react-native.core :as rn]))

(defn separator
  [{:keys [style]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:style
      (merge
       style
       {:background-color (quo.colors/theme-colors
                           quo.colors/neutral-10
                           quo.colors/neutral-80
                           theme)
        :align-self       :stretch
        :height           1})}]))
