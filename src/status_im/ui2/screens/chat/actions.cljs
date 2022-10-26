(ns status-im.ui2.screens.chat.actions
  (:require
   [status-im.chat.models :as chat.models]
   [status-im.chat.models.pin-message :as models.pin-message]
   [status-im.i18n.i18n :as i18n]
   [status-im.constants :as constants]
   [status-im.utils.handlers :refer [<sub >evt]]
   [quo2.components.drawers.action-drawers :as drawer]))

(defn- entry [icon label on-press danger?]
  {:pre [(keyword? icon)
         (string? label)
         (fn? on-press)
         (boolean? danger?)]}
  {:icon     icon
   :label    label
   :on-press on-press
   :danger?  danger?})

(defn hide-sheet-and-dispatch [event]
  (>evt [:bottom-sheet/hide])
  (>evt event))

(defn show-profile-action [chat-id]
  (hide-sheet-and-dispatch  [:chat.ui/show-profile chat-id])
  (>evt [::models.pin-message/load-pin-messages chat-id]))

(defn mark-all-read-action [chat-id]
  (hide-sheet-and-dispatch  [:chat/mark-all-as-read chat-id]))

(defn edit-nickname-action [chat-id]
  (hide-sheet-and-dispatch  [:chat.ui/edit-nickname chat-id]))

(defn mute-chat-action [chat-id]
  (hide-sheet-and-dispatch  [::chat.models/mute-chat-toggled chat-id true]))

(defn unmute-chat-action [chat-id]
  (hide-sheet-and-dispatch  [::chat.models/mute-chat-toggled chat-id false]))

(defn clear-history-action [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/clear-history-pressed chat-id]))

(defn delete-chat-action [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/remove-chat-pressed chat-id]))

(defn mute-chat-entry [muted? chat-id]
  (entry :main-icons2/muted
         (i18n/label
          (if muted?
            :unmute-chat
            :mute-chat))
         (if muted?
           #(unmute-chat-action chat-id)
           #(mute-chat-action chat-id))
         false))

(defn mark-as-read-entry [chat-id]
  (entry :main-icons2/check
         (i18n/label :mark-as-read)
         #(mark-all-read-action chat-id)
         false))

(defn clear-history-entry [chat-id]
  (entry :main-icons2/delete
         (i18n/label :clear-history)
         #(clear-history-action chat-id)
         true))

(defn delete-chat-entry [chat-id]
  (entry :main-icons2/delete
         (i18n/label :delete-chat)
         #(delete-chat-action chat-id)
         true))

(defn view-profile-entry [chat-id]
  (entry :main-icons2/friend
         (i18n/label :view-profile)
         #(show-profile-action chat-id)
         false))

(defn edit-nickname-entry [chat-id]
  (entry :main-icons2/edit
         (i18n/label :edit-nickname)
         #(edit-nickname-action chat-id)
         false))

(defn destructive-actions [chat-id]
  [(clear-history-entry chat-id)
   (delete-chat-entry chat-id)])

(defn notification-actions [muted? chat-id]
  [(mute-chat-entry muted? chat-id)
   (mark-as-read-entry chat-id)])

(defn one-to-one-actions [muted? chat-id]
  [drawer/action-drawer [[(view-profile-entry chat-id)
                          (edit-nickname-entry chat-id)]
                         (notification-actions muted? chat-id)
                         (destructive-actions chat-id)]])

(defn public-chat-actions [muted? chat-id]
  [drawer/action-drawer [(notification-actions muted? chat-id)
                         (destructive-actions chat-id)]])

(defn private-group-chat-actions [muted? chat-id]
  [drawer/action-drawer [(notification-actions muted? chat-id)
                         (destructive-actions chat-id)]])

(defn actions [chat-type chat-id]
  (let [muted? (<sub [:chats/muted chat-id])]
    (case chat-type
      constants/one-to-one-chat-type
      [one-to-one-actions muted? chat-id]
      constants/public-chat-type
      [public-chat-actions muted? chat-id]
      constants/private-group-chat-type
      [private-group-chat-actions muted? chat-id])))
