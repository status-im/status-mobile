(ns status-im.ui.screens.chat.message.sheets
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.message.sheets :as sheets.styles]
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