(ns quo2.components.settings.reorder-item.items.item
  (:require [react-native.core :as rn]
            [quo2.components.settings.reorder-item.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.components.icon :as icon]
            [quo2.foundations.colors :as colors]))

(defn view
  [{:keys
    [title
     subtitle
     image
     image-size
     right-text
     right-icon
     on-press]}]
  [rn/touchable-opacity
   {:on-press on-press
    :style    (merge (style/item-container) (when subtitle style/item-container-extended))}
   [icon/icon :main-icons/drag
    {:color (colors/theme-colors
             colors/neutral-50
             colors/neutral-40)}]
   [rn/view
    {:style style/body-container}
    [rn/view
     {:style style/image-container}
     [rn/image
      {:source image
       :style  (style/image image-size)}]]
    [rn/view
     {:style style/text-container}
     [rn/view
      [text/text
       {:style  style/item-text
        :weight :medium}
       title]
      (when subtitle
        [text/text
         {:style  style/item-subtitle
          :weight :regular}
         subtitle])]
     (when right-text
       [text/text {:style style/right-text} right-text])
     (when right-icon
       [rn/view {:style style/right-icon-container} [icon/icon right-icon (style/right-icon)]])]]
   [icon/icon :tiny-icons/chevron-right (style/chevron)]])
