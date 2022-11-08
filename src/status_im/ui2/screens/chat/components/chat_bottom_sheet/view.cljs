(ns status-im.ui2.screens.chat.components.chat-bottom-sheet.view
  (:require [status-im.i18n.i18n :as i18n]
            [quo.react-native :as rn]
            [status-im.ui2.screens.chat.messages.message :refer [pinned-messages-list]]
            [quo2.components.drawers.action-drawers :refer [divider]]
            [quo2.components.list-items.menu-item :as quo2.menu-item]
            [re-frame.core :as rf]))

(defn hide-sheet-and-dispatch [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(defn chat-bottom-sheet [{:keys [chat-id group-chat]}]
  [rn/view
   (if group-chat
     [quo2.menu-item/menu-item
      {:type                :main
       :title               (i18n/label :t/group-details)
       :accessibility-label "group-details"
       :icon                :members
       :on-press            #(hide-sheet-and-dispatch [:show-group-chat-profile chat-id])}]
     [quo2.menu-item/menu-item
      {:type                :main
       :title               (i18n/label :t/view-profile)
       :accessibility-label "view-profile"
       :icon                :friend
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/show-profile chat-id])}])

   [divider]
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/mark-as-read)
     :accessibility-label "mark-as-read"
     :icon                :correct
     :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-pressed chat-id])}]
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/mute-chat)
     :description         "Muted for 15 minutes"
     :accessibility-label "mute-chat"
     :icon                :muted
     :arrow?              true
     :on-press            #(println "TODO: mute chat")}] ; TODO: issue https://github.com/status-im/status-mobile/issues/14269
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/notifications)
     :description         "Only @mentions"
     :accessibility-label "notifications"
     :icon                :notifications
     :arrow?              true
     :on-press            #(println "TODO: chat notifications")}]
   (when group-chat
     [quo2.menu-item/menu-item
      {:type                :main
       :title               (i18n/label :t/pinned-messages)
       :accessibility-label "pinned-messages"
       :icon                :pin
       :on-press            (fn []
                              (rf/dispatch [:bottom-sheet/hide])
                              (rf/dispatch [:bottom-sheet/show-sheet
                                            {:content #(pinned-messages-list chat-id)}]))}])
   [divider]
   [quo2.menu-item/menu-item
    {:type                :danger
     :title               (i18n/label :t/clear-history)
     :accessibility-label "clear-history"
     :icon                :delete
     :on-press            #(rf/dispatch [:chat.ui/clear-history-pressed chat-id])}]
   (if group-chat
     [quo2.menu-item/menu-item
      {:type                :danger
       :title               (i18n/label :t/leave-and-delete-group)
       :accessibility-label "leave-group"
       :icon                :log-out
       :on-press            #(rf/dispatch [:group-chats.ui/leave-chat-pressed chat-id])}]
     [quo2.menu-item/menu-item
      {:type                :danger
       :title               (i18n/label :t/delete-chat)
       :accessibility-label "delete-chat"
       :icon                :delete
       :on-press            #(rf/dispatch [:chat.ui/remove-chat-pressed chat-id])}])])
