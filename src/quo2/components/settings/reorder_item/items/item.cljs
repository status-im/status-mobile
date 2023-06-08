(ns quo2.components.settings.reorder-item.items.item
  (:require [quo.react-native :as rn] 
            [status-im.ui.components.icons.icons :as icons]
            [quo2.components.settings.reorder-item.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.components.icon :as quo2-icons]))

(defn view
  [{:keys
    [title
     subtitle
     image
     image-size
     right-text
     right-icon]} drag]
  [rn/touchable-opacity
   {:on-long-press       drag
    :delay-long-press    100
    :accessibility-label :chat-drag-handle
    :style               (style/item-container)}
   [icons/icon :main-icons/drag {:style (style/left-icon)}]
   [rn/view
    {:style style/body-container}
    [rn/view
     {:style style/image-container}
     [rn/image
      {:source image
       :style (style/image image-size)}]]
    [rn/view
     {:style style/text-container}
     [rn/view
      [text/text
       {:style style/item-text
        :weight :medium}
       title]
      (when subtitle
        [text/text
         {:style style/item-subtitle
          :weight :regular}
         subtitle])]
     (when right-text
       [text/text {:style style/right-text} right-text])
     (when right-icon
       [rn/view {:style style/right-icon-container} [quo2-icons/icon right-icon (style/right-icon)]])]]
   [icons/tiny-icon :tiny-icons/chevron-right (style/chevron)]])
