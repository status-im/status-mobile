(ns quo2.components.settings.category.settings.view
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.components.settings.settings-item.view :as settings-item]
    [quo2.foundations.colors :as colors]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [quo2.components.settings.category.style :as style]
    [quo2.theme :as quo.theme]))

(defn- category-internal
  [{:keys [label data blur? theme]}]
  [rn/view {:style style/container}
   (when blur?
     [rn/view (style/blur-container) [blur/view (style/blur-view)]])
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
    label]
   [rn/flat-list
    {:data      data
     :style     (style/settings-items theme blur?)
     :render-fn (fn [item] [settings-item/view item])
     :separator [rn/view {:style (style/settings-separator theme blur?)}]}]])

(def settings-category (quo.theme/with-theme category-internal))
