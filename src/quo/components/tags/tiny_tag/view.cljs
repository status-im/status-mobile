(ns quo.components.tags.tiny-tag.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.tags.tiny-tag.style :as style]
    [quo.context :as quo.context]
    [react-native.core :as rn]))

(defn view
  [{:keys [label] :as props}]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style style/main}
     [rn/view {:style (style/inner props theme)}
      [text/text
       {:style  (style/label props theme)
        :weight :medium
        :size   :label
        :align  :center} label]]]))
