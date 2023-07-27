(ns quo2.components.settings.category.view
  (:require [quo2.components.settings.category.settings.view :as settings]
            [quo2.components.settings.category.reorder.view :as reorder]))

(defn category
  [{:keys [list-type] :as props}]
  (if (= list-type :settings)
    [settings/settings-category props]
    [reorder/reorder-category props]))
