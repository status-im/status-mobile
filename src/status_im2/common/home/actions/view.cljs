(ns status-im2.common.home.actions.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [status-im2.common.confirmation-drawer.view :as confirmation-drawer]
            [status-im2.constants :as constants]
            [utils.re-frame :as rf]))

(defn- entry
  [{:keys [icon label on-press danger? sub-label chevron? add-divider? accessibility-label]}]
  {:pre [(keyword? icon)
         (string? label)
         (fn? on-press)
         (boolean? danger?)
         (boolean? chevron?)]}
  {:icon                icon
   :label               label
   :on-press            on-press
   :danger?             danger?
   :sub-label           sub-label
   :right-icon          (when chevron? :i/chevron-right)
   :add-divider?        add-divider?
   :accessibility-label accessibility-label})

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide-and-dispatch #(rf/dispatch event)]))

(defn show-profile-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/show-profile chat-id])
  (rf/dispatch [:pin-message/load-pin-messages chat-id]))

(defn mark-all-read-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat/mark-all-as-read chat-id]))

(defn edit-nickname-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/edit-nickname chat-id]))

(defn mute-chat-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/mute chat-id true]))

(defn unmute-chat-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/mute chat-id false]))

(defn clear-history-action
  [{:keys [chat-id] :as item}]
  (hide-sheet-and-dispatch
   [:bottom-sheet/show-sheet
    {:content (fn []
                (confirmation-drawer/confirmation-drawer
                 {:title               (i18n/label :t/clear-history?)
                  :description         (i18n/label :t/clear-history-confirmation-content)
                  :context             item
                  :accessibility-label :clear-history-confirm
                  :button-text         (i18n/label :t/clear-history)
                  :on-press            #(hide-sheet-and-dispatch [:chat.ui/clear-history chat-id])}))}]))

(defn delete-chat-action
  [{:keys [chat-id] :as item}]
  (hide-sheet-and-dispatch
   [:bottom-sheet/show-sheet
    {:content (fn []
                (confirmation-drawer/confirmation-drawer
                 {:title               (i18n/label :t/delete-chat?)
                  :description         (i18n/label :t/delete-chat-confirmation)
                  :context             item
                  :accessibility-label :delete-chat-confirm
                  :button-text         (i18n/label :t/delete-chat)
                  :on-press            #(hide-sheet-and-dispatch [:chat.ui/remove-chat chat-id])}))}]))

(defn leave-group-action
  [item chat-id]
  (hide-sheet-and-dispatch
   [:bottom-sheet/show-sheet
    {:content (fn []
                (confirmation-drawer/confirmation-drawer
                 {:title               (i18n/label :t/leave-group?)
                  :description         (i18n/label :t/leave-chat-confirmation)
                  :context             item
                  :accessibility-label :leave-group
                  :button-text         (i18n/label :t/leave-group)
                  :on-press            #(do
                                          (rf/dispatch [:navigate-back])
                                          (hide-sheet-and-dispatch [:group-chats.ui/leave-chat-confirmed
                                                                    chat-id]))}))}]))

(defn block-user-action
  [{:keys [public-key] :as item}]
  (hide-sheet-and-dispatch
   [:bottom-sheet/show-sheet
    {:content (fn []
                (confirmation-drawer/confirmation-drawer
                 {:title               (i18n/label :t/block-user?)
                  :description         (i18n/label :t/block-contact-details)
                  :context             item
                  :accessibility-label :block-user
                  :button-text         (i18n/label :t/block-user)
                  :on-press            #(hide-sheet-and-dispatch [:contact.ui/block-contact-confirmed
                                                                  public-key])}))}]))

(defn mute-chat-entry
  [chat-id]
  (let [muted? (rf/sub [:chats/muted chat-id])]
    (entry {:icon                (if muted? :i/muted :i/activity-center)
            :label               (i18n/label
                                  (if muted?
                                    :unmute-chat
                                    :mute-chat))
            :on-press            (if muted?
                                   #(unmute-chat-action chat-id)
                                   #(mute-chat-action chat-id))
            :danger?             false
            :accessibility-label :mute-chat
            :sub-label           nil
            :chevron?            true})))

(defn mark-as-read-entry
  [chat-id]
  (entry {:icon                :i/correct
          :label               (i18n/label :t/mark-as-read)
          :on-press            #(mark-all-read-action chat-id)
          :danger?             false
          :accessibility-label :mark-as-read
          :sub-label           nil
          :chevron?            false
          :add-divider?        true}))

(defn clear-history-entry
  [chat-id]
  (entry {:icon                :i/delete
          :label               (i18n/label :t/clear-history)
          :on-press            #(clear-history-action chat-id)
          :danger?             true
          :sub-label           nil
          :accessibility-label :clear-history
          :chevron?            false
          :add-divider?        true}))

(defn delete-chat-entry
  [item]
  (entry {:icon                :i/delete
          :label               (i18n/label :t/delete-chat)
          :on-press            #(delete-chat-action item)
          :danger?             true
          :accessibility-label :delete-chat
          :sub-label           nil
          :chevron?            false}))

(defn leave-group-entry
  [item extra-data]
  (entry
   {:icon                :i/log-out
    :label               (i18n/label :t/leave-group)
    :on-press            #(leave-group-action item (if extra-data (:chat-id extra-data) (:chat-id item)))
    :danger?             true
    :accessibility-label :leave-group
    :sub-label           nil
    :chevron?            false
    :add-divider?        extra-data}))

(defn view-profile-entry
  [chat-id]
  (entry {:icon                :i/friend
          :label               (i18n/label :t/view-profile)
          :on-press            #(show-profile-action chat-id)
          :danger?             false
          :accessibility-label :view-profile
          :sub-label           nil
          :chevron?            false}))

(defn edit-nickname-entry
  [chat-id]
  (entry {:icon                :i/edit
          :label               (i18n/label :t/edit-nickname)
          :on-press            #(edit-nickname-action chat-id)
          :danger?             false
          :accessibility-label :edit-nickname
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): Requires design input.
(defn edit-name-image-entry
  []
  (entry {:icon                :i/edit
          :label               (i18n/label :t/edit-name-and-image)
          :on-press            #(js/alert "TODO: to be implemented, requires design input")
          :danger?             false
          :accessibility-label :edit-name-and-image
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): Requires design input.
(defn notifications-entry
  [add-divider?]
  (entry {:icon                :i/notifications
          :label               (i18n/label :t/notifications)
          :on-press            #(js/alert "TODO: to be implemented, requires design input")
          :danger?             false
          :sub-label           "All messages" ; TODO: placeholder
          :accessibility-label :manage-notifications
          :chevron?            true
          :add-divider?        add-divider?}))

;; TODO(OmarBasem): Requires design input.
(defn fetch-messages-entry
  []
  (entry {:icon                :i/save
          :label               (i18n/label :t/fetch-messages)
          :on-press            #(js/alert "TODO: to be implemented, requires design input")
          :danger?             false
          :accessibility-label :fetch-messages
          :sub-label           nil
          :chevron?            true}))

(defn remove-from-contacts-entry
  [contact]
  (entry {:icon                :i/remove-user
          :label               (i18n/label :t/remove-from-contacts)
          :on-press            #(hide-sheet-and-dispatch [:contact.ui/remove-contact-pressed contact])
          :danger?             false
          :accessibility-label :remove-from-contacts
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): Requires design input.
(defn rename-entry
  []
  (entry {:icon                :i/edit
          :label               (i18n/label :t/rename)
          :on-press            #(js/alert "TODO: to be implemented, requires design input")
          :danger?             false
          :accessibility-label :rename-contact
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): Requires design input.
(defn show-qr-entry
  []
  (entry {:icon                :i/qr-code
          :label               (i18n/label :t/show-qr)
          :on-press            #(js/alert "TODO: to be implemented, requires design input")
          :danger?             false
          :accessibility-label :show-qr-code
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): to be implemented.
(defn share-profile-entry
  []
  (entry {:icon                :i/share
          :label               (i18n/label :t/share-profile)
          :on-press            #(js/alert "TODO: to be implemented")
          :danger?             false
          :accessibility-label :share-profile
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): to be implemented.
(defn share-group-entry
  []
  (entry {:icon                :i/share
          :label               (i18n/label :t/share)
          :on-press            #(js/alert "TODO: to be implemented")
          :danger?             false
          :accessibility-label :share-group
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): Requires status-go impl.
(defn mark-untrustworthy-entry
  []
  (entry {:icon                :i/alert
          :label               (i18n/label :t/mark-untrustworthy)
          :on-press            #(js/alert "TODO: to be implemented, requires status-go impl.")
          :danger?             true
          :accessibility-label :mark-untrustworthy
          :sub-label           nil
          :chevron?            false
          :add-divider?        true}))

(defn block-user-entry
  [item]
  (entry {:icon                :i/block
          :label               (i18n/label :t/block-user)
          :on-press            #(block-user-action item)
          :danger?             true
          :accessibility-label :block-user
          :sub-label           nil
          :chevron?            false}))

(defn remove-from-group-entry
  [{:keys [public-key]} chat-id]
  (let [username (first (rf/sub [:contacts/contact-two-names-by-identity public-key]))]
    (entry {:icon                :i/placeholder
            :label               (i18n/label :t/remove-user-from-group {:username username})
            :on-press            #(hide-sheet-and-dispatch [:group-chats.ui/remove-member-pressed chat-id
                                                            public-key true])
            :danger?             true
            :accessibility-label :remove-from-group
            :sub-label           nil
            :chevron?            false
            :add-divider?        true})))

(defn group-details-entry
  [chat-id]
  (entry {:icon                :i/members
          :label               (i18n/label :t/group-details)
          :on-press            #(hide-sheet-and-dispatch [:show-group-chat-profile chat-id])
          :danger?             false
          :accessibility-label :group-details
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): to be implemented.
(defn add-members-entry
  []
  (entry {:icon                :i/add-user
          :label               (i18n/label :t/add-members)
          :on-press            #(js/alert "TODO: to be implemented")
          :danger?             false
          :accessibility-label :add-members
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): to be implemented.
(defn manage-members-entry
  []
  (entry {:icon                :i/add-user
          :label               (i18n/label :t/manage-members)
          :on-press            #(js/alert "TODO: to be implemented")
          :danger?             false
          :accessibility-label :manage-members
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): to be implemented.
(defn edit-group-entry
  []
  (entry {:icon                :i/edit
          :label               (i18n/label :t/edit-name-and-image)
          :on-press            #(js/alert "TODO: to be implemented")
          :danger?             false
          :accessibility-label :edit-group
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): to be implemented.
(defn group-privacy-entry
  []
  (entry {:icon                :i/privacy
          :label               (i18n/label :t/change-group-privacy)
          :on-press            #(js/alert "TODO: to be implemented")
          :danger?             false
          :accessibility-label :group-privacy
          :sub-label           nil
          :chevron?            false}))

(defn destructive-actions
  [{:keys [group-chat] :as item}]
  [(clear-history-entry item)
   (if group-chat
     (leave-group-entry item nil)
     (delete-chat-entry item))])

(defn notification-actions
  [{:keys [chat-id group-chat public?]} inside-chat?]
  [(mark-as-read-entry chat-id)
   (mute-chat-entry chat-id)
   (notifications-entry false)
   (when inside-chat?
     (fetch-messages-entry))
   (when (or (not group-chat) public?)
     (show-qr-entry))
   (when-not group-chat
     (share-profile-entry))
   (when public?
     (share-group-entry))])

(defn group-actions
  [{:keys [chat-id admins]} inside-chat?]
  (let [current-pub-key (rf/sub [:multiaccount/public-key])
        admin?          (get admins current-pub-key)]
    [(group-details-entry chat-id)
     (when inside-chat?
       (if admin?
         (manage-members-entry)
         (add-members-entry)))
     (when (and admin? inside-chat?) (edit-group-entry))
     (when (and admin? inside-chat?) (group-privacy-entry))]))

(defn one-to-one-actions
  [{:keys [chat-id] :as item} inside-chat?]
  [quo/action-drawer
   [[(view-profile-entry chat-id)
     (edit-nickname-entry chat-id)]
    (notification-actions item inside-chat?)
    (destructive-actions item)]])

(defn public-chat-actions
  [{:keys [chat-id] :as item} inside-chat?]
  [quo/action-drawer
   [[(group-details-entry chat-id)
     (when inside-chat?
       (add-members-entry))]
    (notification-actions item inside-chat?)
    (destructive-actions item)]])

(defn private-group-chat-actions
  [item inside-chat?]
  [quo/action-drawer
   [(group-actions item inside-chat?)
    (notification-actions item inside-chat?)
    (destructive-actions item)]])

(defn contact-actions
  [{:keys [public-key] :as contact} {:keys [chat-id admin?] :as extra-data}]
  (let [current-pub-key (rf/sub [:multiaccount/public-key])]
    [quo/action-drawer
     [[(view-profile-entry public-key)
       (remove-from-contacts-entry contact)
       (rename-entry)
       (show-qr-entry)
       (share-profile-entry)]
      [(mark-untrustworthy-entry)
       (block-user-entry contact)]
      (when (and admin? chat-id)
        [(if (= current-pub-key public-key)
           (leave-group-entry contact extra-data)
           (remove-from-group-entry contact chat-id))])]]))

(defn actions
  [{:keys [chat-type] :as item} {:keys [inside-chat?] :as extra-data}]
  (case chat-type
    constants/one-to-one-chat-type
    [one-to-one-actions item inside-chat?]
    constants/public-chat-type
    [public-chat-actions item inside-chat?]
    constants/private-group-chat-type
    [private-group-chat-actions item inside-chat?]
    [contact-actions item extra-data]))

(defn group-details-actions
  [{:keys [admins] :as group}]
  (let [current-pub-key (rf/sub [:multiaccount/public-key])
        admin?          (get admins current-pub-key)]
    [quo/action-drawer
     [(when admin? [(edit-name-image-entry)])
      [(notifications-entry admin?)]
      (destructive-actions group)]]))
