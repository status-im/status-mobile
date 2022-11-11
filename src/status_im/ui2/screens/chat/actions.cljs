(ns status-im.ui2.screens.chat.actions
  (:require
   [status-im.chat.models :as chat.models]
   [status-im.chat.models.pin-message :as models.pin-message]
   [status-im.i18n.i18n :as i18n]
   [status-im.constants :as constants]
   [status-im.utils.re-frame :as rf]
   [quo2.components.drawers.action-drawers :as drawer]))

(defn- entry [{:keys [icon label on-press danger? sub-label chevron?]}]
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
   :right-icon (when chevron? :i/chevron-right)})

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

(defn mute-chat-entry [chat-id]
  (let [muted? (rf/sub [:chats/muted chat-id])]
    (entry {:icon      :i/muted
            :label     (i18n/label
                        (if muted?
                          :unmute-chat
                          :mute-chat))
            :on-press  (if muted?
                         #(unmute-chat-action chat-id)
                         #(mute-chat-action chat-id))
            :danger?   false
            :sub-label nil
            :chevron?  true})))

(defn mark-as-read-entry [chat-id]
  (entry {:icon      :i/correct
          :label     (i18n/label :t/mark-as-read)
          :on-press  #(mark-all-read-action chat-id)
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn clear-history-entry [chat-id]
  (entry {:icon      :i/delete
          :label     (i18n/label :t/clear-history)
          :on-press  #(clear-history-action chat-id)
          :danger?   true
          :sub-label nil
          :chevron?  false}))

(defn delete-chat-entry [chat-id]
  (entry {:icon      :i/delete
          :label     (i18n/label :t/delete-chat)
          :on-press  #(delete-chat-action chat-id)
          :danger?   true
          :sub-label nil
          :chevron?  false}))

(defn leave-group-entry [chat-id]
  (entry {:icon      :i/log-out
          :label     (i18n/label :t/leave-group)
          :on-press  #(leave-group-action chat-id)
          :danger?   true
          :sub-label nil
          :chevron?  false}))

(defn view-profile-entry [chat-id]
  (entry {:icon      :i/friend
          :label     (i18n/label :t/view-profile)
          :on-press  #(show-profile-action chat-id)
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn edit-nickname-entry [chat-id]
  (entry {:icon      :i/edit
          :label     (i18n/label :t/edit-nickname)
          :on-press  #(edit-nickname-action chat-id)
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn notifications-entry []
  (entry {:icon      :i/notifications
          :label     (i18n/label :t/notifications)
          :on-press  #(js/alert "TODO: to be implemented, requires design input")
          :danger?   false
          :sub-label "All messages" ; TODO: placeholder
          :chevron?  true}))

(defn fetch-messages-entry []
  (entry {:icon      :i/save
          :label     (i18n/label :t/fetch-messages)
          :on-press  #(js/alert "TODO: to be implemented, requires design input")
          :danger?   false
          :sub-label nil
          :chevron?  true}))

(defn pinned-messages-entry []
  (entry {:icon      :i/pin
          :label     (i18n/label :t/pinned-messages)
          :on-press  #(js/alert "TODO: to be implemented, requires design input")
          :danger?   false
          :sub-label nil
          :chevron?  true}))

(defn remove-from-contacts-entry [contact]
  (entry {:icon      :i/remove-user
          :label     (i18n/label :t/remove-from-contacts)
          :on-press  #(hide-sheet-and-dispatch [:contact.ui/remove-contact-pressed contact])
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn rename-entry []
  (entry {:icon      :i/edit
          :label     (i18n/label :t/rename)
          :on-press  #(js/alert "TODO: to be implemented, requires design input")
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn show-qr-entry []
  (entry {:icon      :i/qr-code
          :label     (i18n/label :t/show-qr)
          :on-press  #(js/alert "TODO: to be implemented, requires design input")
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn share-profile-entry []
  (entry {:icon      :i/share
          :label     (i18n/label :t/share-profile)
          :on-press  #(js/alert "TODO: to be implemented")
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn share-group-entry []
  (entry {:icon      :i/share
          :label     (i18n/label :t/share)
          :on-press  #(js/alert "TODO: to be implemented")
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn mark-untrustworthy-entry []
  (entry {:icon      :i/alert
          :label     (i18n/label :t/mark-untrustworthy)
          :on-press  #(js/alert "TODO: to be implemented, probably requires status-go impl. and design input")
          :danger?   true
          :sub-label nil
          :chevron?  false}))

(defn block-user-entry []
  (entry {:icon      :i/block
          :label     (i18n/label :t/block-user)
          :on-press  #(js/alert "TODO: to be implemented, requires design input")
          :danger?   true
          :sub-label nil
          :chevron?  false}))

(defn group-details-entry [chat-id]
  (entry {:icon      :i/members
          :label     (i18n/label :t/group-details)
          :on-press  #(hide-sheet-and-dispatch [:show-group-chat-profile chat-id])
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn add-members-entry []
  (entry {:icon      :i/add-user
          :label     (i18n/label :t/add-members)
          :on-press  #(js/alert "TODO: to be implemented")
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn manage-members-entry []
  (entry {:icon      :i/add-user
          :label     (i18n/label :t/manage-members)
          :on-press  #(js/alert "TODO: to be implemented")
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn edit-group-entry []
  (entry {:icon      :i/edit
          :label     (i18n/label :t/edit-name-and-image)
          :on-press  #(js/alert "TODO: to be implemented")
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn group-privacy-entry []
  (entry {:icon      :i/privacy
          :label     (i18n/label :t/change-group-privacy)
          :on-press  #(js/alert "TODO: to be implemented")
          :danger?   false
          :sub-label nil
          :chevron?  false}))

(defn destructive-actions [chat-id group-chat]
  [(clear-history-entry chat-id)
   (if group-chat
     (leave-group-entry chat-id)
     (delete-chat-entry chat-id))])

(defn notification-actions [{:keys [chat-id group-chat public?]} inside-chat?]
  [(mark-as-read-entry chat-id)
   (mute-chat-entry chat-id)
   (notifications-entry)
   (if inside-chat?
     (fetch-messages-entry)
     (pinned-messages-entry))
   (when (or (not group-chat) public?)
     (show-qr-entry))
   (when-not group-chat
     (share-profile-entry))
   (when public?
     (share-group-entry))])

(defn group-actions [{:keys [chat-id admins]} inside-chat?]
  (let [current-pk (rf/sub [:multiaccount/public-key])
        admin?     (get admins current-pk)]
    [(group-details-entry chat-id)
     (when inside-chat?
       (if admin?
         (manage-members-entry)
         (add-members-entry)))
     (when (and admin? inside-chat?) (edit-group-entry))
     (when (and admin? inside-chat?) (group-privacy-entry))]))

(defn one-to-one-actions [{:keys [chat-id group-chat] :as item} inside-chat?]
  [drawer/action-drawer [[(view-profile-entry chat-id)
                          (edit-nickname-entry chat-id)]
                         (notification-actions item inside-chat?)
                         (destructive-actions chat-id group-chat)]])

(defn public-chat-actions [{:keys [chat-id group-chat] :as item} inside-chat?]
  [drawer/action-drawer [[(group-details-entry chat-id)
                          (when inside-chat?
                            (add-members-entry))]
                         (notification-actions item inside-chat?)
                         (destructive-actions chat-id group-chat)]])

(defn private-group-chat-actions [{:keys [chat-id group-chat] :as item} inside-chat?]
  [drawer/action-drawer [(group-actions item inside-chat?)
                         (notification-actions item inside-chat?)
                         (destructive-actions chat-id group-chat)]])

(defn contact-actions [{:keys [public-key] :as contact}]
  [drawer/action-drawer [[(view-profile-entry public-key)
                          (remove-from-contacts-entry contact)
                          (rename-entry)
                          (show-qr-entry)
                          (share-profile-entry)]
                         [(mark-untrustworthy-entry)
                          (block-user-entry)]]])

(defn actions [{:keys [chat-type] :as item} inside-chat?]
  (case chat-type
    constants/one-to-one-chat-type
    [one-to-one-actions item inside-chat?]
    constants/public-chat-type
    [public-chat-actions item inside-chat?]
    constants/private-group-chat-type
    [private-group-chat-actions item inside-chat?]
    [contact-actions item]))

