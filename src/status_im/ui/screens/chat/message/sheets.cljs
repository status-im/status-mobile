(ns status-im.ui.screens.chat.message.sheets
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.message.sheets :as sheets.styles]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.list-item.views :as list-item]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn options [chat-id message-id]
  (fn []
    [react/view
     [react/i18n-text {:style sheets.styles/sheet-text :key :message-not-sent}]
     [list-item/list-item
      {:theme               :action
       :title               :t/resend-message
       :icon                :main-icons/refresh
       :accessibility-label :resend-message-button
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/resend-message chat-id message-id])}]
     [list-item/list-item
      {:theme               :action-destructive
       :title               :t/delete-message
       :icon                :main-icons/delete
       :accessibility-label :delete-transaction-button
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/delete-message chat-id message-id])}]]))

(defn message-long-press [{:keys [message-id content]}]
  (fn []
    [react/view
     [list-item/list-item
      {:theme    :action
       :title    :t/message-reply
       :icon     :main-icons/reply
       :on-press #(hide-sheet-and-dispatch [:chat.ui/reply-to-message message-id])}]
     [list-item/list-item
      {:theme    :action
       :title    :t/sharing-copy-to-clipboard
       :icon     :main-icons/copy
       :on-press (fn []
                   (re-frame/dispatch [:bottom-sheet/hide-sheet])
                   (react/copy-to-clipboard (:text content)))}]
     (when-not platform/desktop?
       [list-item/list-item
        {:theme    :action
         :title    :t/sharing-share
         :icon     :main-icons/share
         :on-press (fn []
                     (re-frame/dispatch [:bottom-sheet/hide-sheet])
                     (list-selection/open-share {:message (:text content)}))}])]))

(defn sticker-long-press [{:keys [message-id]}]
  (fn []
    [react/view
     [list-item/list-item
      {:theme    :action
       :title    :t/message-reply
       :icon     :main-icons/reply
       :on-press #(hide-sheet-and-dispatch [:chat.ui/reply-to-message message-id])}]]))
