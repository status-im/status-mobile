(ns status-im.ui.screens.chat.extensions.views
  (:require [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]))

(defn extensions-view
  []
  [react/view
   {:style {:background-color colors/white
            :flex             1}}
   [react/view {:style {:flex-direction :row}}
    [react/touchable-highlight
     {:on-press #(re-frame/dispatch [:wallet/prepare-transaction-from-chat])}
     [react/view
      {:width              128
       :height             128
       :justify-content    :space-between
       :padding-horizontal 10
       :padding-vertical   12
       :background-color   (colors/alpha colors/purple 0.2)
       :border-radius      16
       :margin-left        8}
      [react/view
       {:background-color colors/purple
        :width            40
        :height           40
        :border-radius    20
        :align-items      :center
        :justify-content  :center}
       [icons/icon :main-icons/send {:color colors/white}]]
      [react/text {:typography :medium} (i18n/label :t/send-transaction)]]]
    [react/touchable-highlight
     {:on-press #(re-frame/dispatch [:wallet/prepare-request-transaction-from-chat])}
     [react/view
      {:width              128
       :height             128
       :justify-content    :space-between
       :padding-horizontal 10
       :padding-vertical   12
       :background-color   (colors/alpha colors/orange 0.2)
       :border-radius      16
       :margin-left        8}
      [react/view
       {:background-color colors/orange
        :width            40
        :height           40
        :border-radius    20
        :align-items      :center
        :justify-content  :center}
       [icons/icon :main-icons/receive {:color colors/white}]]
      [react/text {:typography :medium} (i18n/label :t/request-transaction)]]]]])
