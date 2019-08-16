(ns status-im.ui.components.list-item.views
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-item.styles :as styles]
            [status-im.utils.image :as utils.image]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.components.action-button.styles :as st]))

; type - optional :default , :small

; accessories - optional vector of :chevron, :check, :more or component or string

; theme - optional :default, :wallet, :action, :action-red

(defn list-item [{:keys [title subtitle accessories image image-path icon type theme on-press error content] :or {type :default theme :default}}]
  (let [small? (= :small type)]
    [react/touchable-highlight {:on-press on-press :disabled (not on-press)}
     [react/view {:style (styles/container small?)}
      ;;Image
      (when image
        [react/view {:margin-left 16}
         (if (vector? image)
           image
           [image])])
      ;;Icon
      (when icon
        [react/view {:margin-left 16}
         [react/view (st/action-button-icon-container (when (= theme :action-red) colors/red-transparent-10))
          [icons/icon icon (if (= theme :action-red) st/action-button-label-red st/action-button-label)]]])
      (when image-path
        [react/view {:margin-left 16}
         [react/image {:source (utils.image/source image-path)
                       :style  (styles/photo 40)}]])
      ;;Title
      (when title
        [react/view {:style {:margin-left 16 :margin-right 16}}
         [react/text {:style           (merge (styles/title small? subtitle)
                                              (when (= theme :action) st/action-button-label)
                                              (when (= theme :action-red) st/action-button-label-red))
                      :number-of-lines 1
                      :ellipsize-mode  :tail}
          title]
         ;;Subtitle
         (when subtitle
           [react/text {:style           styles/subtitle
                        :number-of-lines 1
                        :ellipsize-mode  :tail}
            subtitle])])
      ;;Content
      (when content
        (if (vector? content)
          content
          [content]))
      [react/view {:flex 1}]
      ;;Accessories
      (for [accessory accessories]
        (when-not (nil? accessory)
          (with-meta
            (cond
              (= :chevron accessory)
              [react/view {:margin-right 8}
               [icons/icon :main-icons/next {:color colors/gray-transparent-40}]]
              (= :check accessory)
              [react/view {:margin-right 16}
               [icons/icon :main-icons/check {:color colors/gray}]]
              (= :more accessory)
              [react/view {:margin-right 16}
               [icons/icon :main-icons/more]]
              :else
              [react/view {:margin-right 16 :flex-shrink 1}
               (cond
                 (string? accessory)
                 [react/text {:style styles/accessory-text :number-of-lines 1}
                  accessory]
                 (vector? accessory)
                 accessory
                 :else
                 [accessory])])
            {:key accessory})))
      (when error
        [tooltip/tooltip error styles/error])]]))
