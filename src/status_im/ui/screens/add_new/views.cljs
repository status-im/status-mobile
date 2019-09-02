(ns status-im.ui.screens.add-new.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.list-item.views :as list-item]))

(defn- options-list []
  [react/view {:flex 1}
   [list-item/list-item
    {:theme               :action
     :title               (i18n/label :t/start-new-chat)
     :accessibility-label :start-1-1-chat-button
     :icon                :main-icons/add-contact
     :on-press            #(re-frame/dispatch [:navigate-to :new-chat])}]
   [common/list-separator]
   [list-item/list-item
    {:theme               :action
     :title               (i18n/label :t/start-group-chat)
     :accessibility-label :start-group-chat-button
     :icon                :main-icons/group-chat
     :on-press            #(re-frame/dispatch [:contact.ui/start-group-chat-pressed])}]
   [common/list-separator]
   [list-item/list-item
    {:theme               :action
     :title               (i18n/label :t/new-public-group-chat)
     :accessibility-label :join-public-chat-button
     :icon                :main-icons/public-chat
     :on-press            #(re-frame/dispatch [:navigate-to :new-public-chat])}]
   (when-not platform/desktop?
     [common/list-separator]
     [list-item/list-item
      {:theme               :action
       :title               (i18n/label :t/invite-friends)
       :accessibility-label :invite-friends-button
       :icon                :main-icons/share
       :on-press            #(list-selection/open-share {:message (i18n/label :t/get-status-at)})}]
     [common/list-separator]
     [list-item/list-item
      {:theme               :action
       :title               (i18n/label :t/scan-qr)
       :accessibility-label :scan-qr-code-button
       :icon                :main-icons/qr
       :on-press            #(re-frame/dispatch [:qr-scanner.ui/scan-qr-code-pressed
                                                 {:toolbar-title (i18n/label :t/scan-qr)}
                                                 :handle-qr-code])}])])

(defn add-new []
  [react/view {:flex 1 :background-color :white}
   [status-bar/status-bar]
   [toolbar/simple-toolbar (i18n/label :t/new)]
   [common/separator]
   [options-list]])
