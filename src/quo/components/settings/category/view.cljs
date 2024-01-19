(ns quo.components.settings.category.view
  (:require
    [quo.components.settings.category.reorder.view :as reorder]
    [quo.components.settings.category.settings.view :as settings]
    [react-native.pure :as rn.pure]
    [reagent.core :as reagent]))

(defn category
  [{:keys [list-type] :as props}]
  (if (= list-type :settings)
    (rn.pure/func settings/settings-category props)
    (reagent/as-element [reorder/reorder-category props])))
