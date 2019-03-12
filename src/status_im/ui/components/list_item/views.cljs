(ns status-im.ui.components.list-item.views
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-item.styles :as styles]
            [status-im.utils.image :as utils.image]))

; type - optional :default , :small

; accessories - optional vector of :chevron, :check or component or string

; theme - optional :default, :wallet

(defn list-item [{:keys [title subtitle accessories image image-path type theme on-press] :or {type :default theme :default}}]
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
      [react/view {:style {:margin-left 16 :flex 1}}
       [react/text {:style (styles/title small? subtitle)}
        title]
       ;;Subtitle
       (when subtitle
         [react/text {:style           styles/subtitle
                      :number-of-lines 1
                      :ellipsize-mode  :tail}
          subtitle])]
      ;;Accessories
      (for [accessory accessories]
        (with-meta
          (cond
            (string? accessory)
            [react/text {:style styles/accessory-text}
             accessory]
            (= :chevron accessory)
            [icons/icon :main-icons/next {:color colors/gray-transparent-40}]
            (= :check accessory)
            [icons/icon :main-icons/check {:color colors/gray}]
            :else accessory)
          {:key accessory}))]]))
