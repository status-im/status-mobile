(ns legacy.status-im.ui.screens.browser.eip3085.sheet
  (:require
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon.screen]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.copyable-text :as copyable-text]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.browser.styles :as styles]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(views/defview permissions-panel
  [dapp-name message-id params]
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
     [react/text {:style styles/permissions-panel-title-label :number-of-lines 2}
      (str "\"" dapp-name "\" Allow this site to add a network?")]
     [react/text {:style styles/permissions-panel-description-label :number-of-lines 4}
      "This will allow this network to be used within Status.Status does not verify custom networks.Learn about scams and network security risks."]
     [react/scroll-view
      [copyable-text/copyable-text-view
       {:copied-text (:name (:new-network params))}
       [list.item/list-item
        {:size                :small
         :accessibility-label :network-name
         :title               "Network Name"
         :accessory           :text
         :accessory-text      (:name (:new-network params))}]]
      [copyable-text/copyable-text-view
       {:copied-text (get-in params [:new-network :config :UpstreamConfig :URL])}
       [list.item/list-item
        {:size                :small
         :accessibility-label :network-url
         :title               "Network URL"
         :accessory           :text
         :accessory-text      (get-in params [:new-network :config :UpstreamConfig :URL])}]]
      [copyable-text/copyable-text-view
       {:copied-text (str (get-in params [:new-network :config :NetworkId]))}
       [list.item/list-item
        {:size                :small
         :accessibility-label :network-id
         :title               "Chain ID"
         :accessory           :text
         :accessory-text      (str (get-in params [:new-network :config :NetworkId]))}]]]
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
