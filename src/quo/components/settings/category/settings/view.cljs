(ns quo.components.settings.category.settings.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.settings-item.view :as settings-item]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.pure :as prn]))

(defn- category-internal
  [{:keys [label data] :as props}]
  (prn/view #js {:style (style/container label)}
   (when label
     (prn/text
      #js {:style  (style/label props)}
      label))
   (prn/view #js {:style (style/settings-items props)}
    (for [item data]
      ;(prn/fragment #js {:key item})
      (settings-item/view item)
      #_(when-not (= item (last data))
          (prn/view #js {:style (style/settings-separator props)}))))))

(def settings-category category-internal);(quo.theme/with-theme category-internal))
