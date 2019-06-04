(ns status-im.ui.screens.wallet.collectibles.etheremon.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]
            [status-im.ui.components.svgimage :as svgimage]
            [status-im.ui.screens.wallet.collectibles.views :as collectibles]))

(defmethod collectibles/render-collectible :EMONA [_ {:keys [user_defined_name image class_id]}]
  [react/view {:style styles/details}
   [react/view {:style styles/details-text}
    [react/view {:flex 1}
     [svgimage/svgimage {:style  styles/details-image
                         :source {:uri image
                                  :k   2}}]]
    [react/view {:flex 1 :justify-content :center}
     [react/text {:style styles/details-name}
      user_defined_name]]]
   [action-button/action-button
    {:label               (i18n/label :t/view-etheremon)
     :icon                :main-icons/address
     :icon-opts           {:color colors/blue}
     :accessibility-label :open-collectible-button
     :on-press            #(re-frame/dispatch [:extensions/open-url
                                               (str "https://www.etheremon.com/#/mons/" class_id)])}]])
