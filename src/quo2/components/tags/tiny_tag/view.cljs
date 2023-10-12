(ns quo2.components.tags.tiny-tag.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.theme :as quo.theme]
            [quo2.components.tags.tiny-tag.style :as style]
            [react-native.core :as rn]))

(defn- view-internal
  [{:keys [label] :as props}]
  [rn/view {:style style/main}
   [rn/view {:style (style/inner props)}
    [text/text
     {:style  (style/label props)
      :weight :medium
      :size   :label
      :align  :center} label]]])

(def view (quo.theme/with-theme view-internal))
