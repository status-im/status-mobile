(ns legacy.status-im.ui.screens.browser.eip3085.sheet
  (:require
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon.screen]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.browser.styles :as styles]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(views/defview permissions-panel
  [_dapp-name message-id params]
  (views/letsubs [{:keys [dapp? dapp]} [:get-current-browser]]
    [react/view {}
     [react/view styles/permissions-panel-icons-container
      (if dapp?
        [chat-icon.screen/dapp-icon-permission dapp 40]
        [react/view styles/permissions-panel-dapp-icon-container
         [icons/icon :main-icons/dapp {:color colors/gray}]])
      [react/view {:margin-left 8 :margin-right 4}
       [react/view styles/dot]]
      [react/view {:margin-right 4}
       [react/view styles/dot]]
      [react/view {:margin-right 8}
       [react/view styles/dot]]
      [react/view styles/permissions-panel-ok-icon-container
       [icons/icon :tiny-icons/tiny-check styles/permissions-panel-ok-ico]]
      [react/view {:margin-left 8 :margin-right 4}
       [react/view styles/dot]]
      [react/view {:margin-right 4}
       [react/view styles/dot]]
      [react/view {:margin-right 8}
       [react/view styles/dot]]
      [react/view styles/permissions-panel-wallet-icon-container
       [icons/icon :main-icons/wallet {:color colors/white}]]]
     [react/view
      {:style {:flex-direction    :row
               :justify-content   :center
               :margin-horizontal 8
               :margin-top        24}}
      [react/view
       {:flex              1
        :margin-horizontal 8}
       [quo/button
        {:theme    :negative
         :on-press #(re-frame/dispatch [:eip3085.ui/dapp-permission-denied message-id params])}
        (i18n/label :t/deny)]]
      [react/view
       {:flex              1
        :margin-horizontal 8}
       [quo/button
        {:theme    :positive
         :style    {:margin-horizontal 8}
         :on-press #(re-frame/dispatch [:eip3085.ui/dapp-permission-allowed message-id params])}
        (i18n/label :t/allow)]]]]))
