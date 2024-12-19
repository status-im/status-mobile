(ns status-im.common.home.actions.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [status-im.common.confirmation-drawer.view :as confirmation-drawer]
    [status-im.common.mute-drawer.view :as mute-drawer]
    [status-im.common.muting.helpers :refer [format-mute-till]]
    [status-im.config :as config]
    [status-im.constants :as constants]
    [status-im.contexts.chat.actions.view :as chat-actions]
    [status-im.contexts.chat.contacts.drawers.nickname-drawer.view :as nickname-drawer]
    [status-im.contexts.communities.actions.chat.view :as communities-chat-actions]
    [utils.i18n :as i18n]
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
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch event))

(defn show-profile-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/show-profile chat-id])
  (rf/dispatch [:pin-message/load-pin-messages chat-id]))

(defn mark-all-read-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat/mark-all-as-read chat-id]))

(defn edit-nickname-action
  [contact]
  (hide-sheet-and-dispatch
   [:show-bottom-sheet
    {:content (fn []
                [nickname-drawer/nickname-drawer
                 {:title               (i18n/label :t/add-nickname-title)
                  :description         (i18n/label :t/nickname-visible-to-you)
                  :contact             contact
                  :accessibility-label :edit-nickname}])}]))

(defn mute-chat-action
  [chat-id chat-type muted?]
  (hide-sheet-and-dispatch [:show-bottom-sheet
                            {:content (fn []
                                        [mute-drawer/mute-drawer
                                         {:id                  chat-id
                                          :community?          false
                                          :muted?              (not muted?)
                                          :chat-type           chat-type
                                          :accessibility-label :mute-community-title}])}]))

(defn unmute-chat-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/mute chat-id false]))

(defn clear-history-action
  [{:keys [chat-id] :as item}]
  (hide-sheet-and-dispatch
   [:show-bottom-sheet
    {:content (fn []
                [confirmation-drawer/confirmation-drawer
                 {:title               (i18n/label :t/clear-history?)
                  :description         (i18n/label :t/clear-history-confirmation-content)
                  :context             item
                  :accessibility-label :clear-history-confirm
                  :button-text         (i18n/label :t/clear-history)
                  :on-press            #(hide-sheet-and-dispatch [:chat.ui/clear-history chat-id])}])}]))

(defn close-chat-action
  [{:keys [chat-id] :as item} inside-chat?]
  (hide-sheet-and-dispatch
   [:show-bottom-sheet
    {:content (fn []
                [confirmation-drawer/confirmation-drawer
                 {:title               (i18n/label :t/close-chat)
                  :description         (i18n/label :t/close-chat-confirmation)
                  :context             item
                  :accessibility-label :close-chat-confirm
                  :button-text         (i18n/label :t/confirm)
                  :close-button-text   (i18n/label :t/cancel)
                  :on-press            (fn []
                                         (hide-sheet-and-dispatch [:chat.ui/close-and-remove-chat
                                                                   chat-id])
                                         (when inside-chat?
                                           (rf/dispatch [:navigate-back])))}])}]))

(defn leave-group-action
  [item chat-id]
  (hide-sheet-and-dispatch
   [:show-bottom-sheet
    {:content (fn []
                [confirmation-drawer/confirmation-drawer
                 {:title               (i18n/label :t/leave-group?)
                  :description         (i18n/label :t/leave-chat-confirmation)
                  :context             item
                  :accessibility-label :leave-group
                  :button-text         (i18n/label :t/leave-group)
                  :on-press            (fn []
                                         (hide-sheet-and-dispatch [:group-chats.ui/leave-chat-confirmed
                                                                   chat-id])
                                         (rf/dispatch [:navigate-back]))}])}]))

(defn block-user-action
  [{:keys [public-key] :as item}]
  (hide-sheet-and-dispatch
   [:show-bottom-sheet
    {:content (fn []
                [confirmation-drawer/confirmation-drawer
                 {:title               (i18n/label :t/block-user?)
                  :description         (i18n/label :t/block-contact-details)
                  :context             item
                  :accessibility-label :block-user
                  :button-text         (i18n/label :t/block-user)
                  :on-press            #(hide-sheet-and-dispatch [:contact/block-contact
                                                                  public-key])}])}]))

(defn mute-chat-entry
  [chat-id chat-type muted-till]
  (let [muted? (rf/sub [:chats/muted chat-id])]
    (entry {:icon                (if muted? :i/activity-center :i/muted)
            :label               (i18n/label
                                  (if muted?
                                    :unmute-chat
                                    :mute-chat))
            :sub-label           (when (and muted? (some? muted-till))
                                   (i18n/label :t/muted-until {:duration (format-mute-till muted-till)}))
            :on-press            (if muted?
                                   #(unmute-chat-action chat-id)
                                   #(mute-chat-action chat-id chat-type muted?))
            :danger?             false
            :accessibility-label :mute-chat
            :chevron?            (not muted?)})))

(defn mark-as-read-entry
  [chat-id needs-divider?]
  (entry {:icon                :i/mark-as-read
          :label               (i18n/label :t/mark-as-read)
          :on-press            #(mark-all-read-action chat-id)
          :danger?             false
          :accessibility-label :mark-as-read
          :sub-label           nil
          :chevron?            false
          :add-divider?        needs-divider?}))

(defn clear-history-entry
  [chat needs-divider?]
  (entry {:icon                :i/delete
          :label               (i18n/label :t/clear-history)
          :on-press            #(clear-history-action chat)
          :danger?             true
          :sub-label           nil
          :accessibility-label :clear-history
          :chevron?            false
          :add-divider?        needs-divider?}))

(defn close-chat-entry
  [item inside-chat? needs-divider?]
  (entry {:icon                :i/close-circle
          :label               (i18n/label :t/close-chat)
          :on-press            #(close-chat-action item inside-chat?)
          :danger?             true
          :accessibility-label :close-chat
          :sub-label           nil
          :chevron?            false
          :add-divider?        needs-divider?}))

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
  (entry {:icon                :i/profile
          :label               (i18n/label :t/view-profile)
          :on-press            #(show-profile-action chat-id)
          :danger?             false
          :accessibility-label :view-profile
          :sub-label           nil
          :chevron?            false}))

(defn edit-nickname-entry
  [chat-id]
  (let [{:keys [nickname public-key secondary-name]
         :as   contact} (select-keys (rf/sub [:contacts/contact-by-address chat-id])
                                     [:primary-name :nickname :public-key :secondary-name])
        no-nickname?    (string/blank? nickname)]
    (entry
     {:icon                (if no-nickname?
                             :i/edit
                             :i/delete)
      :label               (i18n/label (if no-nickname?
                                         :t/add-nickname-title
                                         :t/remove-nickname))
      :on-press            (fn []
                             (if no-nickname?
                               (edit-nickname-action contact)
                               (do
                                 (rf/dispatch [:hide-bottom-sheet])
                                 (rf/dispatch [:toasts/upsert
                                               {:id   :remove-nickname
                                                :type :positive
                                                :text (i18n/label
                                                       :t/remove-nickname-toast
                                                       {:secondary-name secondary-name})}])
                                 (rf/dispatch [:contacts/update-nickname public-key ""]))))
      :danger?             false
      :accessibility-label :add-nickname
      :sub-label           nil
      :chevron?            false})))

(defn edit-name-image-entry
  [chat-id]
  (entry {:icon                :i/edit
          :label               (i18n/label :t/edit-name-and-image)
          :on-press            #(rf/dispatch [:open-modal :screen/group-update chat-id])
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
          :sub-label           (i18n/label :t/all-messages)
          :accessibility-label :manage-notifications
          :chevron?            true
          :add-divider?        add-divider?}))


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

(defn show-qr-entry
  [public-key]
  (entry {:icon                :i/qr-code
          :label               (i18n/label :t/show-qr)
          :on-press            (fn []
                                 (rf/dispatch [:universal-links/generate-profile-url
                                               {:public-key public-key
                                                :on-success #(rf/dispatch [:open-modal
                                                                           :share-contact])}]))
          :danger?             false
          :accessibility-label :show-qr-code
          :sub-label           nil
          :chevron?            false}))

(defn share-profile-entry
  [public-key]
  (entry {:icon                :i/share
          :label               (i18n/label :t/share-profile)
          :on-press            (fn []
                                 (rf/dispatch [:universal-links/generate-profile-url
                                               {:public-key public-key
                                                :on-success #(rf/dispatch [:open-share
                                                                           {:options {:message %}}])}]))
          :danger?             false
          :accessibility-label :share-profile
          :sub-label           nil
          :chevron?            false}))

;; TODO(OmarBasem): to be implemented.chat/check-channel-muted?
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
  (let [[primary-name _] (rf/sub [:contacts/contact-two-names-by-identity public-key])]
    (entry {:icon                :i/placeholder
            :label               (i18n/label :t/remove-user-from-group {:username primary-name})
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
          :on-press            (fn []
                                 (rf/dispatch [:chats-list/load-chat chat-id])
                                 (rf/dispatch [:pin-message/load-pin-messages chat-id])
                                 (rf/dispatch [:hide-bottom-sheet])
                                 (rf/dispatch [:navigate-to :screen/group-details chat-id]))
          :danger?             false
          :accessibility-label :group-details
          :sub-label           nil
          :chevron?            false}))

(defn add-manage-members-entry
  [chat-id admin?]
  (entry {:icon                :i/add-user
          :label               (i18n/label (if admin? :t/manage-members :t/add-members))
          :on-press            #(rf/dispatch [:open-modal :screen/group-add-manage-members chat-id])
          :danger?             false
          :accessibility-label :manage-members
          :sub-label           nil
          :chevron?            false}))

(defn edit-group-entry
  [chat-id]
  (entry {:icon                :i/edit
          :label               (i18n/label :t/edit-name-and-image)
          :on-press            #(rf/dispatch [:open-modal :screen/group-update chat-id])
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
  [{:keys [group-chat chat-type] :as item} inside-chat?]
  [(when (not group-chat)
     (close-chat-entry item inside-chat? (not= chat-type constants/public-chat-type)))
   (when-not (= chat-type constants/public-chat-type)
     (clear-history-entry item group-chat))
   (when group-chat
     (leave-group-entry item nil))])

(defn notification-actions
  [{:keys [chat-id public? chat-type muted-till]} inside-chat? needs-divider?]
  [(mark-as-read-entry chat-id needs-divider?)
   (mute-chat-entry chat-id chat-type muted-till)
   (when config/show-not-implemented-features?
     (notifications-entry false))
   (when (and config/fetch-messages-enabled? inside-chat?)
     (chat-actions/fetch-messages chat-id))
   (when public?
     (when config/show-not-implemented-features?
       (share-group-entry)))])

(defn group-actions
  [{:keys [chat-id admins]} inside-chat?]
  (let [current-pub-key (rf/sub [:multiaccount/public-key])
        admin?          (get admins current-pub-key)]
    [(group-details-entry chat-id)
     (when inside-chat?
       (add-manage-members-entry chat-id admin?))
     (when (and admin? inside-chat?)
       (edit-group-entry chat-id))
     (when (and admin? inside-chat?)
       (when config/show-not-implemented-features?
         (group-privacy-entry)))]))

(defn one-to-one-actions
  [{:keys [chat-id] :as item} inside-chat?]
  [quo/action-drawer
   [[(view-profile-entry chat-id)]
    (notification-actions item inside-chat? true)
    (destructive-actions item inside-chat?)]])

(defn public-chat-actions
  [item inside-chat?]
  [quo/action-drawer
   [(destructive-actions item inside-chat?)]])

(defn private-group-chat-actions
  [item inside-chat?]
  [quo/action-drawer
   (let [show-group-actions? (:group-chat item)]
     [(when show-group-actions?
        (group-actions item inside-chat?))
      (notification-actions item inside-chat? show-group-actions?)
      (destructive-actions item inside-chat?)])])

(defn contact-actions
  [{:keys [public-key added?] :as contact} {:keys [chat-id admin?] :as extra-data}]
  (let [current-pub-key (rf/sub [:multiaccount/public-key])]
    [quo/action-drawer
     [[(view-profile-entry public-key)
       (when-not (= current-pub-key public-key)
         (edit-nickname-entry public-key))
       (show-qr-entry public-key)
       (share-profile-entry public-key)]
      [(when-not (= current-pub-key public-key)
         (when config/show-not-implemented-features?
           (mark-untrustworthy-entry)))
       (when added? (remove-from-contacts-entry contact))
       (when-not (= current-pub-key public-key) (block-user-entry contact))]
      (when (and admin? chat-id)
        [(if (= current-pub-key public-key)
           (leave-group-entry contact extra-data)
           (remove-from-group-entry contact chat-id))])]]))

(defn chat-actions
  ([{:keys [chat-type] :as chat} inside-chat? hide-show-members?]
   (condp = chat-type
     constants/one-to-one-chat-type
     [one-to-one-actions chat inside-chat?]
     constants/private-group-chat-type
     [private-group-chat-actions chat inside-chat?]
     constants/public-chat-type
     [public-chat-actions chat inside-chat?]
     constants/community-chat-type
     [communities-chat-actions/actions chat inside-chat? hide-show-members?]
     nil))
  ([chat inside-chat?]
   (chat-actions chat inside-chat? nil)))

(defn group-details-actions
  [{:keys [chat-id admins] :as group}]
  (let [current-pub-key (rf/sub [:multiaccount/public-key])
        admin?          (get admins current-pub-key)]
    [quo/action-drawer
     [(when admin?
        [(edit-name-image-entry chat-id)])
      [(when config/show-not-implemented-features?
         (notifications-entry admin?))]
      (destructive-actions group false)]]))
