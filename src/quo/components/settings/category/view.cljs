(ns quo.components.settings.category.view
  (:require
    [quo.components.settings.category.reorder.view :as reorder]
    [quo.components.settings.category.settings.view :as settings]))

(defn category
  [{:keys [list-type] :as props}]
  (if (= list-type :settings)
    [settings/settings-category props]
    [reorder/reorder-category props]))
