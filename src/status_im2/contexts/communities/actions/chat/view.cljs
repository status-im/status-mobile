(ns status-im2.contexts.communities.actions.chat.view
  (:require [quo2.core :as quo]
            [status-im2.common.not-implemented :as not-implemented]
            [utils.i18n :as i18n]))

(defn- action-view-members-and-details
  []
  {:icon                :i/members
   :accessibility-label :chat-view-members-and-details
   :label               (i18n/label :t/view-channel-members-and-details)
   :on-press            not-implemented/alert})

(defn- action-token-requirements
  []
  {:icon                :i/token
   :right-icon          :i/chevron-right
   :accessibility-label :chat-view-token-requirements
   :on-press            not-implemented/alert
   :label               (i18n/label :t/view-token-gating)})

(defn- action-mark-as-read
  []
  {:icon                :i/mark-as-read
   :accessibility-label :chat-mark-as-read
   :on-press            not-implemented/alert
   :label               (i18n/label :t/mark-as-read)})

(defn- action-toggle-muted
  []
  {:icon                :i/muted
   :right-icon          :i/chevron-right
   :accessibility-label :chat-toggle-muted
   :on-press            not-implemented/alert
   :label               (i18n/label :t/mute-channel)})

(defn- action-notification-settings
  []
  {:icon                :i/notifications
   :right-icon          :i/chevron-right
   :accessibility-label :chat-notification-settings
   :on-press            not-implemented/alert
   :label               (i18n/label :t/notification-settings)
   :sub-label           (i18n/label :t/only-mentions)})

(defn- action-pinned-messages
  []
  {:icon                :i/pin
   :right-icon          :i/chevron-right
   :accessibility-label :chat-pinned-messages
   :on-press            not-implemented/alert
   :label               (i18n/label :t/pinned-messages)})

(defn- action-fetch-messages
  []
  {:icon                :i/download
   :right-icon          :i/chevron-right
   :accessibility-label :chat-fetch-messages
   :on-press            not-implemented/alert
   :label               (i18n/label :t/fetch-messages)})

(defn- action-invite-people
  []
  {:icon                :i/add-user
   :accessibility-label :chat-invite-people
   :on-press            not-implemented/alert
   :label               (i18n/label :t/invite-people-from-contacts)})

(defn- action-qr-code
  []
  {:icon                :i/qr-code
   :accessibility-label :chat-show-qr-code
   :on-press            not-implemented/alert
   :label               (i18n/label :t/show-qr)})

(defn- action-share
  []
  {:icon                :i/share
   :accessibility-label :chat-share
   :on-press            not-implemented/alert
   :label               (i18n/label :t/share-channel)})

(defn actions
  [{:keys [locked?]} inside-chat?]
  (cond
    locked?
    [quo/action-drawer
     [[(action-invite-people)
       (action-token-requirements)
       (action-qr-code)
       (action-share)]]]

    (and (not inside-chat?) (not locked?))
    [quo/action-drawer
     [[(action-view-members-and-details)
       (action-mark-as-read)
       (action-toggle-muted)
       (action-notification-settings)
       (action-pinned-messages)
       (action-invite-people)
       (action-qr-code)
       (action-share)]]]

    (and inside-chat? (not locked?))
    [quo/action-drawer
     [[(action-view-members-and-details)
       (action-token-requirements)
       (action-mark-as-read)
       (action-toggle-muted)
       (action-notification-settings)
       (action-fetch-messages)
       (action-invite-people)
       (action-qr-code)
       (action-share)]]]

    :else nil))
