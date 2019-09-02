(ns status-im.ui.screens.wallet.collectibles.cryptokitties.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]
            [status-im.ui.screens.wallet.collectibles.views :as collectibles]
            [status-im.ui.components.svgimage :as svgimage]
            [status-im.ui.components.list-item.views :as list-item]))

(defmethod collectibles/render-collectible :CK [_ {:keys [id name bio image_url]}]
  [react/view {:style styles/details}
   [react/view {:style styles/details-text}
    [svgimage/svgimage {:style styles/details-image
                        :source {:uri image_url}}]
    [react/view {:flex 1}
     [react/text {:style styles/details-name}
      (or name (i18n/label :t/cryptokitty-name {:id id}))]
     [react/text {:number-of-lines 3
                  :ellipsize-mode :tail}
      bio]]]
   [list-item/list-item
    {:theme               :action
     :title               :t/view-cryptokitties
     :icon                :main-icons/address
     :accessibility-label :open-collectible-button
     :on-press            #(re-frame/dispatch [:open-collectible-in-browser
                                               (str "https://www.cryptokitties.co/kitty/" id)])}]])
