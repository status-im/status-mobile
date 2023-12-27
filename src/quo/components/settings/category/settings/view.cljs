(ns quo.components.settings.category.settings.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.settings-item.view :as settings-item]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- category-internal
  [{:keys [label data style] :as props}]
  [rn/view {:style (merge (style/container label) style)}
   (when label
     [text/text
      {:weight :medium
       :size   :paragraph-2
       :style  (style/label props)}
      label])
   [rn/view {:style (style/settings-items props)}
    (for [item data]
      ^{:key item}
      [:<>
       [settings-item/view item]
       (when-not (= item (last data))
         [rn/view {:style (style/settings-separator props)}])])]])

(def settings-category (quo.theme/with-theme category-internal))
