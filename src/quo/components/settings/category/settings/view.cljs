(ns quo.components.settings.category.settings.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.settings-item.view :as settings-item]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- category-internal
  [{:keys [label data container-style settings-type] :as props}]
  (let [settings-item (filter identity data)]
    [rn/view {:style (merge (style/container label) container-style)}
     (when label
       [text/text
        {:weight :medium
         :size   :paragraph-2
         :style  (style/label props)}
        label])
     [rn/view
      {:style (merge (style/settings-items props)
                     (when (= settings-type :page-setting)
                       style/page-setting))}
      (for [item settings-item]
        ^{:key item}
        [:<>
         [settings-item/view item]
         (when-not (= item (last settings-item))
           [rn/view {:style (style/settings-separator props)}])])]]))

(def settings-category (quo.theme/with-theme category-internal))
