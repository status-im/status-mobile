(ns quo.components.settings.category.settings.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.settings-item.view :as settings-item]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn settings-category
  [{:keys [label data blur? container-style]}]
  (let [theme          (quo.theme/use-theme)
        settings-items (remove nil? data)
        last-index     (dec (count settings-items))]
    [rn/view {:style (merge (style/container label) container-style)}
     (when label
       [text/text
        {:weight :medium
         :size   :paragraph-2
         :style  (style/label blur? theme)}
        label])
     [rn/view {:style (style/settings-items blur? theme)}
      (map-indexed
       (fn [index item]
         ^{:key (str label (:title item))}
         [:<>
          [settings-item/view item]
          (when-not (= last-index index)
            [rn/view {:style (style/settings-separator blur? theme)}])])
       settings-items)]]))