(ns quo2.components.settings.data-item.content.left-side
  (:require [react-native.core :as rn]
            [quo2.components.settings.data-item.style :as style]
            [quo2.components.settings.data-item.content.subtitle :as left-subtitle]
            [quo2.components.settings.data-item.content.loading :as left-loading]
            [quo2.components.settings.data-item.content.title :as left-title]))

(defn view
  [theme title status size blur? description icon subtitle label icon-color]
  [rn/view {:style style/left-side}
   [left-title/view title label size]
   (if (= status :loading)
     [left-loading/view size blur?]
     [left-subtitle/view theme size description icon icon-color blur? subtitle])])
