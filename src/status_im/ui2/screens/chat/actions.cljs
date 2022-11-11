(ns status-im.ui2.screens.chat.actions
  (:require
   [status-im.chat.models :as chat.models]
   [status-im.chat.models.pin-message :as models.pin-message]
   [status-im.i18n.i18n :as i18n]
   [status-im.constants :as constants]
   [status-im.utils.re-frame :as rf]
   [quo2.components.drawers.action-drawers :as drawer]))

(defn- entry [icon label on-press danger? sub-label chevron?]
  {:pre [(keyword? icon)
         (string? label)
         (fn? on-press)
         (boolean? danger?)
         (boolean? chevron?)]}
  {:icon       icon
   :label      label
   :on-press   on-press
   :danger?    danger?
   :sub-label  sub-label
   :right-icon (when chevron? :main-icons2/chevron-right)})

(defn hide-sheet-and-dispatch [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(defn show-profile-action [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/show-profile chat-id])
  (rf/dispatch [::models.pin-message/load-pin-messages chat-id]))

(defn mark-all-read-action [chat-id]
  (hide-sheet-and-dispatch [:chat/mark-all-as-read chat-id]))

(defn edit-nickname-action [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/edit-nickname chat-id]))

(defn mute-chat-action [chat-id]
  (hide-sheet-and-dispatch [::chat.models/mute-chat-toggled chat-id true]))

(defn unmute-chat-action [chat-id]
  (hide-sheet-and-dispatch [::chat.models/mute-chat-toggled chat-id false]))

(defn clear-history-action [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/clear-history-pressed chat-id]))

(defn delete-chat-action [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/remove-chat-pressed chat-id]))

(defn leave-group-action [chat-id]
  (hide-sheet-and-dispatch [:group-chats.ui/leave-chat-pressed chat-id]))

(defn mute-chat-entry [muted? chat-id]
  (entry :i/muted
         (i18n/label
          (if muted?
            :unmute-chat
            :mute-chat))
         (if muted?
           #(unmute-chat-action chat-id)
           #(mute-chat-action chat-id))
         false
         nil
         true))

(defn mark-as-read-entry [chat-id]
  (entry :i/check
         (i18n/label :mark-as-read)
         #(mark-all-read-action chat-id)
         false
         nil
         false))

(defn clear-history-entry [chat-id]
  (entry :i/delete
         (i18n/label :clear-history)
         #(clear-history-action chat-id)
         true
         nil
         false))

(defn delete-chat-entry [chat-id]
  (entry :i/delete
         (i18n/label :delete-chat)
         #(delete-chat-action chat-id)
         true
         nil
         false))

(defn leave-group-entry [chat-id]
  (entry :main-icons2/log-out
         (i18n/label :leave-group)
         #(leave-group-action chat-id)
         true
         nil
         false))

(defn view-profile-entry [chat-id]
  (entry :i/friend
         (i18n/label :view-profile)
         #(show-profile-action chat-id)
         false
         nil
         false))

(defn edit-nickname-entry [chat-id]
  (entry :i/edit
         (i18n/label :edit-nickname)
         #(edit-nickname-action chat-id)
         false
         nil
         false))

(defn notifications-entry []
  (entry :main-icons2/notifications
         (i18n/label :notifications)
         #(js/alert "TODO: to be implemented, requires design input")
         false
         "All messages" ; placeholder
         true))

(defn fetch-messages-entry []
  (entry :main-icons2/save
         (i18n/label :fetch-messages)
         #(js/alert "TODO: to be implemented, requires design input")
         false
         nil
         true))

(defn remove-from-contacts-entry [contact]
  (entry :main-icons2/remove-user
         (i18n/label :remove-from-contacts)
         #(hide-sheet-and-dispatch [:contact.ui/remove-contact-pressed contact])
         false
         nil
         false))

(defn rename-entry []
  (entry :main-icons2/edit
         (i18n/label :rename)
         #(js/alert "TODO: to be implemented, requires design input")
         false
         nil
         false))

(defn show-qr-entry []
  (entry :main-icons2/qr-code
         (i18n/label :show-qr)
         #(js/alert "TODO: to be implemented, requires design input")
         false
         nil
         false))

(defn share-profile-entry []
  (entry :main-icons2/share
         (i18n/label :share-profile)
         #(js/alert "TODO: to be implemented")
         false
         nil
         false))

(defn mark-untrustworthy-entry []
  (entry :main-icons2/alert
         (i18n/label :mark-untrustworthy)
         #(js/alert "TODO: to be implemented, probably requires status-go impl. and design input")
         true
         nil
         false))

(defn block-user-entry []
  (entry :main-icons2/block
         (i18n/label :block-user)
         #(js/alert "TODO: to be implemented, requires design input")
         true
         nil
         false))

(defn group-details-entry [chat-id]
  (entry :main-icons2/members
         (i18n/label :group-details)
         #(hide-sheet-and-dispatch [:show-group-chat-profile chat-id])
         false
         nil
         false))

(defn add-members-entry []
  (entry :main-icons2/add-user
         (i18n/label :add-members)
         #(js/alert "TODO: to be implemented")
         false
         nil
         false))

(defn destructive-actions [chat-id group-chat]
  [(clear-history-entry chat-id)
   (if group-chat
     (leave-group-entry chat-id)
     (delete-chat-entry chat-id))])

(defn notification-actions [muted? chat-id]
  [(mute-chat-entry muted? chat-id)
   (notifications-entry)
   (fetch-messages-entry)
   (mark-as-read-entry chat-id)
   (show-qr-entry)
   (share-profile-entry)])

(defn one-to-one-actions [muted? chat-id group-chat]
  [drawer/action-drawer [[(view-profile-entry chat-id)
                          (edit-nickname-entry chat-id)]
                         (notification-actions muted? chat-id)
                         (destructive-actions chat-id group-chat)]])

(defn public-chat-actions [muted? chat-id group-chat]
  [drawer/action-drawer [[(group-details-entry chat-id)
                          (add-members-entry)]
                         (notification-actions muted? chat-id)
                         (destructive-actions chat-id group-chat)]])

(defn private-group-chat-actions [muted? chat-id group-chat]
  [drawer/action-drawer [[(group-details-entry chat-id)
                          (add-members-entry)]
                         (notification-actions muted? chat-id)
                         (destructive-actions chat-id group-chat)]])

(defn contact-actions [{:keys [public-key] :as contact}]
  [drawer/action-drawer [[(view-profile-entry public-key)
                          (remove-from-contacts-entry contact)
                          (rename-entry)
                          (show-qr-entry)
                          (share-profile-entry)]
                         [(mark-untrustworthy-entry)
                          (block-user-entry)]]])

(defn actions [{:keys [chat-type chat-id group-chat] :as item}]
  (let [muted? (rf/sub [:chats/muted chat-id])]
    (case chat-type
      constants/one-to-one-chat-type
      [one-to-one-actions muted? chat-id group-chat]
      constants/public-chat-type
      [public-chat-actions muted? chat-id group-chat]
      constants/private-group-chat-type
      [private-group-chat-actions muted? chat-id group-chat]
      [contact-actions item])))

