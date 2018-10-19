(ns status-im.ui.screens.wallet.collectibles.superrare.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]
            [status-im.ui.screens.wallet.collectibles.views :as collectibles]))

(defmethod collectibles/render-collectible :SUPR [_ {tokenId :tokenId {:keys [description name imageUri]} :metadata}]
  [react/view {:style styles/details}
   [react/view {:style styles/details-text}
    [react/image {:style  (merge {:resize-mode :contain :width 100 :height 100} styles/details-image)
                  :source {:uri imageUri
                           :k   1.4}}]
    [react/view {:flex 1 :justify-content :center}
     [react/text {:style styles/details-name}
      name]
     [react/text
      description]]]
   [action-button/action-button
    {:label               (i18n/label :t/view-superrare)
     :icon                :icons/address
     :icon-opts           {:color colors/blue}
     :accessibility-label :open-collectible-button
     :on-press            #(re-frame/dispatch [:open-collectible-in-browser
                                               (str "https://superrare.co/artwork/" name "-" tokenId)])}]])
