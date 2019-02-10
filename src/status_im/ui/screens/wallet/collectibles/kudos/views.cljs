(ns status-im.ui.screens.wallet.collectibles.kudos.views
  (:require [status-im.ui.screens.wallet.collectibles.views :as collectibles]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]
            [status-im.ui.components.svgimage :as svgimage]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]))

(defmethod collectibles/render-collectible :KDO [_ {:keys [external_url description name image]}]
  [react/view {:style styles/details}
   [react/view {:style styles/details-text}
    [svgimage/svgimage {:style  styles/details-image
                        :source {:uri image
                                 :k   1.4}}]
    [react/view {:flex 1 :justify-content :center}
     [react/text {:style styles/details-name}
      name]
     [react/text
      description]]]
   [action-button/action-button
    {:label               (i18n/label :t/view-gitcoin)
     :icon                :main-icons/address
     :icon-opts           {:color colors/blue}
     :accessibility-label :open-collectible-button
     :on-press            #(re-frame/dispatch [:open-collectible-in-browser external_url])}]])
