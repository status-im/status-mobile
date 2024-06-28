(ns quo.components.selectors.disclaimer.view
  (:require
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.selectors.disclaimer.style :as style]
    [quo.components.selectors.selectors.view :as selectors]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [checked? blur? accessibility-label container-style on-change icon customization-color]} label]
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
        :on-change           on-change
        :customization-color customization-color}]
      [text/text
       {:size  :paragraph-2
        :style style/text}
       label]
      (when icon
        [rn/view {:style style/icon-container}
         [icons/icon icon
          {:accessibility-label :disclaimer-icon
           :color               (if blur?
                                  (colors/white-opa-70)
                                  (colors/theme-colors colors/neutral-50
                                                       colors/neutral-40
                                                       theme))}]])]]))
