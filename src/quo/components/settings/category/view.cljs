(ns quo.components.settings.category.view
  (:require
    [quo.components.settings.category.data-item.view :as data-item]
    [quo.components.settings.category.reorder.view :as reorder]
    [quo.components.settings.category.settings.view :as settings]))

(defn category
  [{:keys [list-type] :as props}]
  (condp = list-type
    :settings  [settings/settings-category props]
    :data-item [data-item/view props]
    :reorder   [reorder/reorder-category props]
    nil))
