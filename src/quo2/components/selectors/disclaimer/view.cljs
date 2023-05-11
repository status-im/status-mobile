(ns quo2.components.selectors.disclaimer.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.selectors.disclaimer.style :as style]
            [quo2.components.selectors.selectors.view :as selectors]
            [react-native.core :as rn]))

(defn view
  [{:keys [checked? blur? accessibility-label container-style on-change]} label]
  [rn/touchable-opacity
   {:on-press            on-change
    :accessibility-label "disclaimer-touchable-opacity"}
   [rn/view {:style (merge container-style (style/container blur?))}
    [selectors/checkbox
     {:accessibility-label accessibility-label
      :blur?               blur?
      :checked?            checked?
      :on-change           on-change}]
    [text/text
     {:size  :paragraph-2
      :style style/text}
     label]]])
