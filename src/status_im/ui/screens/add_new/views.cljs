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
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]))

(defn- options-list [{:keys [dev-mode?]}]
  [react/view action-button.styles/actions-list
   [action-button/action-button
    {:label               (i18n/label :t/start-new-chat)
     :accessibility-label :start-1-1-chat-button
     :icon                :main-icons/add-contact
     :icon-opts           {:color colors/blue}
     :on-press            #(re-frame/dispatch [:navigate-to :new-chat])}]
   [action-button/action-separator]
   [action-button/action-button
    {:label               (i18n/label :t/start-group-chat)
     :accessibility-label :start-group-chat-button
     :icon                :main-icons/group-chat
     :icon-opts           {:color colors/blue}
     :on-press            #(re-frame/dispatch [:contact.ui/start-group-chat-pressed])}]
   [action-button/action-separator]
   [action-button/action-button
    {:label               (i18n/label :t/new-public-group-chat)
     :accessibility-label :join-public-chat-button
     :icon                :main-icons/public-chat
     :icon-opts           {:color colors/blue}
     :on-press            #(re-frame/dispatch [:navigate-to :new-public-chat])}]
   (when-not platform/desktop?
     [action-button/action-separator]
     [action-button/action-button
      {:label               (i18n/label :t/invite-friends)
       :accessibility-label :invite-friends-button
       :icon                :main-icons/share
       :icon-opts           {:color colors/blue}
       :on-press            #(list-selection/open-share {:message (i18n/label :t/get-status-at)})}]
     [action-button/action-separator]
     [action-button/action-button
      {:label               (i18n/label :t/scan-qr)
       :accessibility-label :scan-qr-code-button
       :icon                :main-icons/qr
       :icon-opts           {:color colors/blue}
       :on-press            #(re-frame/dispatch [:qr-scanner.ui/scan-qr-code-pressed
                                                 {:toolbar-title (i18n/label :t/scan-qr)}
                                                 :handle-qr-code])}])])

(views/defview add-new []
  (views/letsubs [account     [:account/account]
                  device-UUID [:get-device-UUID]]
    [react/view {:flex 1 :background-color :white}
     [status-bar/status-bar]
     [toolbar/simple-toolbar (i18n/label :t/new)]
     [common/separator]
     [options-list account]]))
