(ns quo.components.settings.category.settings.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.settings-item.view :as settings-item]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- category-internal
  [{:keys [label data] :as props}]
  [rn/view {:style (style/container label)}
   (when label
     [text/text
      {:weight :medium
       :size   :paragraph-2
       :style  (style/label props)}
      label])
   [rn/flat-list
    {:data      data
     :style     (style/settings-items props)
     :render-fn settings-item/view
     :separator [rn/view {:style (style/settings-separator props)}]}]])

(def settings-category (quo.theme/with-theme category-internal))
