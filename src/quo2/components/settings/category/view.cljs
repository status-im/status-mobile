(ns quo2.components.settings.category.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [quo2.components.settings.category.style :as style]))

(defn category
  [{:keys [label data]}]
  [rn/view {:style style/container}
   [quo/text
    {:weight :medium
     :size   :paragraph-2
     :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}} label]
   [rn/flat-list
    {:data      data
     :style     style/items
     :render-fn (fn [item] [quo/settings-list item])
     :separator [rn/view {:style style/separator}]}]])
