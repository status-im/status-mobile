(ns quo.components.settings.category.settings.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.settings-item.view :as settings-item]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn settings-category
  [{:keys [label data container-style] :as props}]
  (let [theme         (quo.theme/use-theme)
        settings-item (filter identity data)]
    [rn/view {:style (merge (style/container label) container-style)}
     (when label
       [text/text
        {:weight :medium
         :size   :paragraph-2
         :style  (style/label props theme)}
        label])
     [rn/view {:style (style/settings-items props theme)}
      (for [item settings-item]
        ^{:key item}
        [:<>
         [settings-item/view item]
         (when-not (= item (last settings-item))
           [rn/view {:style (style/settings-separator props)}])])]]))
