(ns quo2.components.settings.data-item.content.left-side
  (:require [react-native.core :as rn]
            [quo2.components.settings.data-item.style :as style]
            [quo2.components.settings.data-item.content.subtitle :as left-subtitle]
            [quo2.components.settings.data-item.content.loading :as left-loading]
            [quo2.components.settings.data-item.content.title :as left-title]))

(defn view
  [{:keys [theme title status size blur? description icon subtitle label icon-color]}]
  [rn/view {:style style/left-side}
   [left-title/view
    {:title title
     :label label
     :size  size
     :theme theme}]
   (if (= status :loading)
     [left-loading/view
      {:size  size
       :blur? blur?
       :theme theme}]
     [left-subtitle/view
      {:theme       theme
       :size        size
       :description description
       :icon        icon
       :icon-color  icon-color
       :blur?       blur?
       :subtitle    subtitle}])])
