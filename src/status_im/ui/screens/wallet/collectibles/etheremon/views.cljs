(ns status-im.ui.screens.wallet.collectibles.etheremon.views
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]
            [status-im.ui.components.svgimage :as svgimage]
            [status-im.ui.screens.wallet.collectibles.views :as collectibles]
            [status-im.ui.components.list-item.views :as list-item]))

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
   [list-item/list-item
    {:theme               :action
     :title               :t/view-etheremon
     :icon                :main-icons/address
     :accessibility-label :open-collectible-button
     :on-press            #(re-frame/dispatch [:open-collectible-in-browser
                                               (str "https://www.etheremon.com/#/mons/" class_id)])}]])
