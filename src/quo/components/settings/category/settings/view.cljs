(ns quo.components.settings.category.settings.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.settings-item.view :as settings-item]
    [quo.theme :as quo.theme]
    [react-native.pure :as rn.pure]))

(defn settings-category
  [{:keys [label data container-style blur?]}]
  (let [theme (quo.theme/use-theme)
        items (filter identity data)]
    (rn.pure/view
     {:style (merge (style/container label) container-style)}
     (when label
       (text/text
        {:weight :medium
         :size   :paragraph-2
         :style  (style/label theme blur?)}
        label))
     (rn.pure/view
      {:style (style/settings-items theme blur?)}
      (for [item items]
        (rn.pure/fragment
         {:key (:title item)}
         (settings-item/view item)
         (when-not (= item (last data))
           (rn.pure/view {:style (style/settings-separator theme blur?)}))))))))
