(ns quo.components.settings.category.data-item.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.data-item.view :as data-item]
    [quo.context :as quo.context]
    [react-native.core :as rn]))

(defn view
  [{:keys [label data container-style blur?]}]
  (let [theme     (quo.context/use-theme)
        last-item (rn/use-memo #(last data) [data])]
    [rn/view {:style [(style/container label) container-style]}
     (when label
       [text/text
        {:weight :medium
         :size   :paragraph-2
         :style  (style/label blur? theme)}
        label])
     [rn/view {:style (style/settings-items blur? theme)}
      (for [item data
            ;; NOTE: overwriting the background of the data-item in favor of the category bg
            :let [data-item-container-style (-> item :container-style (assoc :background-color nil))
                  data-item-props           (assoc item
                                                   :blur?           blur?
                                                   :container-style data-item-container-style)]]
        ^{:key item}
        [:<>
         [data-item/view data-item-props]
         (when-not (= item last-item)
           [rn/view {:style (style/settings-separator blur? theme)}])])]]))
