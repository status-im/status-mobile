(ns quo2.components.settings.category.view
  (:require
    [quo2.components.settings.category.reorder.view :as reorder]
    [quo2.components.settings.category.settings.view :as settings]))

(defn category
  [{:keys [list-type] :as props}]
  (if (= list-type :settings)
    [settings/settings-category props]
    [reorder/reorder-category props]))
