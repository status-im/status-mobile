(ns quo.components.tags.network-tags.view
  (:require
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as text]
    [quo.components.tags.network-tags.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- view-internal
  [{:keys [title networks status theme blur?] :or {status :default}}]
  [rn/view
   {:style (style/container {:status status
                             :theme  theme
                             :blur?  blur?})}
   [preview-list/view
    {:type   :network
     :number (count networks)
     :size   :size-16} networks]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/title-style {:status status
                                 :theme  theme})}
    title]])

(def view (quo.theme/with-theme view-internal))
