(ns status-im.ui.screens.desktop.main.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.screens.desktop.main.tabs.profile.views :as profile.views]
            [status-im.ui.screens.desktop.main.tabs.home.views :as home.views]
            [status-im.ui.screens.desktop.main.styles :as styles]
            [status-im.ui.screens.desktop.main.chat.views :as chat.views]
            [status-im.ui.screens.desktop.main.add-new.views :as add-new.views]
            [status-im.ui.screens.help-center.views :as help-center.views]
            [status-im.ui.components.desktop.tabs :as tabs]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]))

(views/defview status-view []
  [react/view {:style {:flex 1 :background-color "#eef2f5" :align-items :center :justify-content :center}}
   [react/text {:style {:font-size 18 :color "#939ba1"}}
    "Status.im"]])

(views/defview tab-views []
  (views/letsubs [tab [:get-in [:desktop/desktop :tab-view-id]]]
    (let [component (case tab
                      :profile profile.views/profile-data
                      :home home.views/chat-list-view-wrapper
                      react/view)]
      [react/view {:style {:flex 1}}
       [component]])))

(views/defview popup-view []
  (views/letsubs [popup [:get-in [:desktop :popup]]]
    (when popup
      [react/view {:style styles/absolute}
       [react/touchable-highlight {:on-press #(re-frame/dispatch [:set-in [:desktop :popup] nil])
                                   :style    {:flex 1}}
        [react/view]]
       [react/view {:style styles/absolute}
        [popup]]])))

(views/defview main-view []
  (views/letsubs [view-id [:get :view-id]]
    (let [component (case view-id
                      :chat         chat.views/chat-view
                      :desktop/new-one-to-one  add-new.views/new-one-to-one
                      :desktop/new-public-chat add-new.views/new-public-chat
                      :desktop/new-group-chat add-new.views/new-group-chat
                      :qr-code profile.views/qr-code
                      :advanced-settings profile.views/advanced-settings
                      :chat-settings profile.views/chat-settings
                      :help-center help-center.views/help-center
                      :installations profile.views/installations
                      :chat-profile chat.views/chat-profile
                      :backup-recovery-phrase profile.views/backup-recovery-phrase
                      status-view)]
      [react/view {:style {:flex 1}}
       [component]])))

(views/defview main-views []
  [react/view {:style styles/main-views}
   [react/view {:style styles/left-sidebar}
    [react/view {:style {:flex 1}}
     [tab-views]]
    [tabs/main-tabs]]
   [react/view {:style styles/pane-separator}]
   [main-view]])
