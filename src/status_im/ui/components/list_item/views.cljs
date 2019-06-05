(ns status-im.ui.components.list-item.views
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-item.styles :as styles]
            [status-im.utils.image :as utils.image]
            [status-im.ui.components.tooltip.views :as tooltip]))

; type - optional :default , :small

; accessories - optional vector of :chevron, :check or component or string

; theme - optional :default, :wallet

(defn list-item [{:keys [title subtitle accessories image image-path type theme on-press error] :or {type :default theme :default}}]
  (let [small? (= :small type)]
    [react/touchable-highlight {:on-press on-press :disabled (not on-press)}
     [react/view {:style (styles/container small?)}
      ;;Image
      (when image
        [react/view {:margin-left 16}
         (if (vector? image)
           image
           [image])])
      (when image-path
        [react/view {:margin-left 16}
         [react/image {:source (utils.image/source image-path)
                       :style  (styles/photo 40)}]])
      ;;Title
      [react/view {:style {:margin-left 16 :margin-right 16}}
       [react/text {:style           (styles/title small? subtitle)
                    :number-of-lines 1
                    :ellipsize-mode  :tail}
        title]
       ;;Subtitle
       (when subtitle
         [react/text {:style           styles/subtitle
                      :number-of-lines 1
                      :ellipsize-mode  :tail}
          subtitle])]
      ;;Accessories
      [react/view {:flex 1}]
      (for [accessory accessories]
        (with-meta
          (cond
            (= :chevron accessory)
            [react/view
             [icons/icon :main-icons/next {:color colors/gray-transparent-40}]]
            (= :check accessory)
            [react/view
             [icons/icon :main-icons/check {:color colors/gray}]]
            :else
            [react/view {:padding-right 8 :flex-shrink 1}
             (cond
               (string? accessory)
               [react/text {:style styles/accessory-text}
                accessory]
               (vector? accessory)
               accessory
               :else
               [accessory])])
          {:key accessory}))
      (when error
        [tooltip/tooltip error styles/error])]]))
