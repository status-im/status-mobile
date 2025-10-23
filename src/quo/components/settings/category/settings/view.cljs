(ns quo.components.settings.category.settings.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.settings-item.view :as settings-item]
    [quo.context :as quo.context]
    [react-native.core :as rn]))

(defn settings-category
  [{:keys [label data blur? container-style customization-color]}]
  (let [theme          (quo.context/use-theme)
        settings-items (remove nil? data)
        last-index     (dec (count settings-items))]
    [rn/view {:style [(style/container label) container-style]}
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
          [settings-item/view
           (assoc item
                  :customization-color customization-color
                  :blur?               blur?)]
          (when-not (= last-index index)
            [rn/view {:style (style/settings-separator blur? theme)}])])
       settings-items)]]))
