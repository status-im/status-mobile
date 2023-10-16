(ns quo2.components.settings.reorder-item.items.item
  (:require
    [quo2.components.icon :as icon]
    [quo2.components.markdown.text :as text]
    [quo2.components.settings.reorder-item.style :as style]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]))

(defn- view-internal
  [{:keys
    [title
     subtitle
     image
     image-size
     right-text
     right-icon
     on-press
     theme]}
   blur?
   drag]
  [rn/touchable-opacity
   {:on-press      on-press
    :on-long-press drag
    :style         (merge (style/item-container blur?) (when subtitle style/item-container-extended))}
   [icon/icon :main-icons/drag
    {:color (colors/theme-colors
             colors/neutral-50
             colors/neutral-40)}]
   [rn/view
    {:style style/body-container}
    [rn/view
     {:style style/image-container}
     [fast-image/fast-image
      {:source image
       :style  (style/image image-size)}]]
    [rn/view
     {:style style/text-container}
     [rn/view
      [text/text
       {:weight :medium}
       title]
      (when subtitle
        [text/text
         {:style style/item-subtitle
          :size  :paragraph-2}
         subtitle])]
     (when right-text
       [text/text {:style style/right-text} right-text])
     (when right-icon
       [rn/view {:style style/right-icon-container} [icon/icon right-icon (style/right-icon)]])]]
   [icon/icon :tiny-icons/chevron-right (style/chevron theme)]])


(def view (quo.theme/with-theme view-internal))
