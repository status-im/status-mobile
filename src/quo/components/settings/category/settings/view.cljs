(ns quo.components.settings.category.settings.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.settings-item.view :as settings-item]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- category-internal
  [{:keys [label data blur? container-style theme]}]
  [rn/view {:style (merge (style/container label) container-style)}
   (when label
     [text/text
      {:weight :medium
       :size   :paragraph-2
       :style  {:color (if blur?
                         colors/white-opa-40
                         (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}}
      label])
   [rn/flat-list
    {:data      data
     :style     (style/settings-items theme blur?)
     :render-fn settings-item/view
     :separator [rn/view {:style (style/settings-separator theme blur?)}]}]])

(def settings-category (quo.theme/with-theme category-internal))
