(ns status-im.ui.screens.add-new.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.components.common.styles :as common.styles]
            [status-im.ui.components.toolbar.view :as toolbar]))

(defn- options-list [{:keys [address]}]
  [react/view action-button.styles/actions-list
   [action-button/action-button
    {:label     (i18n/label :t/start-new-chat)
     :icon      :icons/newchat
     :icon-opts {:color colors/blue}
     :on-press  #(re-frame/dispatch [:navigate-to :new-chat])}]
   [action-button/action-separator]
   [action-button/action-button
    {:label     (i18n/label :t/start-group-chat)
     :icon      :icons/contacts
     :icon-opts {:color colors/blue}
     :on-press  #(re-frame/dispatch [:open-contact-toggle-list :chat-group])}]
   [action-button/action-separator]
   [action-button/action-button
    {:label     (i18n/label :t/new-public-group-chat)
     :icon      :icons/public
     :icon-opts {:color colors/blue}
     :on-press  #(re-frame/dispatch [:navigate-to :new-public-chat])}]
   [action-button/action-separator]
   [action-button/action-button
    {:label     (i18n/label :t/open-dapp)
     :icon      :icons/address
     :icon-opts {:color colors/blue}
     :on-press  #(re-frame/dispatch [:navigate-to :open-dapp])}]
   [action-button/action-separator]
   [action-button/action-button
    {:label     (i18n/label :t/invite-friends)
     :icon      :icons/share
     :icon-opts {:color colors/blue}
     :on-press  #(list-selection/open-share {:message (i18n/label :t/get-status-at {:address address})})}]])

(views/defview ^:theme add-new []
  (views/letsubs [account  [:get-current-account]]
    [react/view common.styles/flex
     [toolbar/simple-toolbar (i18n/label :t/new)]
     [common/separator]
     [options-list account]]))
