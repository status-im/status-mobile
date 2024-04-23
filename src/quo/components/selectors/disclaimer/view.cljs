(ns quo.components.selectors.disclaimer.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.selectors.disclaimer.style :as style]
    [quo.components.selectors.selectors.view :as selectors]
    [quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [checked? blur? accessibility-label container-style on-change]} label]
  (let [theme (quo.theme/use-theme)]
    [rn/touchable-without-feedback
     {:on-press            on-change
      :accessibility-label :disclaimer-touchable-opacity}
     [rn/view {:style (merge container-style (style/container blur? theme))}
      [selectors/view
       {:type                :checkbox
        :accessibility-label accessibility-label
        :blur?               blur?
        :checked?            checked?
        :on-change           on-change}]
      [text/text
       {:size  :paragraph-2
        :style style/text}
       label]]]))
