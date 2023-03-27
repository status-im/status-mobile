(ns quo2.components.selectors.disclaimer.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.selectors.disclaimer.style :as style]
            [quo2.components.selectors.selectors :as selectors]
            [react-native.core :as rn]))

(defn view
  [{:keys [checked? on-change accessibility-label container-style]} label]
  [rn/view
   {:style (merge container-style (style/container))}
   [selectors/checkbox
    {:accessibility-label accessibility-label
     :on-change           on-change
     :checked?            checked?}]
   [text/text
    {:size  :paragraph-2
     :style style/text}
    label]])
