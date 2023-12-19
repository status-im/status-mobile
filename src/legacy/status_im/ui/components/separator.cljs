(ns legacy.status-im.ui.components.separator
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [react-native.core :as react]))

(defn separator
  [{:keys [color style]}]
  [react/view
   {:style
    (merge
     style
     {:background-color (colors/get-color (or color :ui-01))
      :align-self       :stretch
      :height           1})}])
