(ns status-im.ui.screens.wallet.collectibles.cryptokitties.views
  (:require [status-im.thread :as status-im.thread]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]
            [status-im.ui.screens.wallet.collectibles.views :as collectibles]
            [status-im.ui.components.svgimage :as svgimage]))

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
   [action-button/action-button
    {:label               (i18n/label :t/view-cryptokitties)
     :icon                :icons/address
     :icon-opts           {:color colors/blue}
     :accessibility-label :open-collectible-button
     :on-press            #(status-im.thread/dispatch
                            [:open-collectible-in-browser
                             (str "https://www.cryptokitties.co/kitty/" id)])}]])
