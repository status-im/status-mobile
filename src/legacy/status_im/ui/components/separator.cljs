(ns legacy.status-im.ui.components.separator
  (:require
    [react-native.core :as react]
    [legacy.status-im.ui.components.colors :as colors]))

(defn separator
  [{:keys [color style]}]
  [react/view
   {:style
    (merge
     style
     {:background-color (colors/get-color (or color :ui-01))
      :align-self       :stretch
      :height           1})}])
