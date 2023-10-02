(ns quo2.components.tags.network-tags.view
  (:require [quo2.components.list-items.preview-list.view :as preview-list]
            [quo2.components.tags.network-tags.style :as style]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]
            [quo2.theme :as quo.theme]))

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
