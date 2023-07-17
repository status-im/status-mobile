(ns status-im2.contexts.communities.actions.chat.view
  (:require [quo2.core :as quo]
            [status-im2.common.not-implemented :as not-implemented]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im2.common.muting.helpers :refer [format-mute-till]]
            [status-im2.common.mute-chat-drawer.view :as mute-chat-drawer]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch event))

(defn- mute-channel-action
  [chat-id chat-type]
  (hide-sheet-and-dispatch [:show-bottom-sheet
                            {:content (fn []
                                        [mute-chat-drawer/mute-chat-drawer chat-id
                                         :mute-chat-for-duration chat-type])}]))

(defn- unmute-channel-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/mute chat-id false 0]))

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
  [id muted? muted-till chat-type]
  (let [muted (and muted? (some? muted-till))]
    (cond-> {:icon                :i/muted
             :accessibility-label :chat-toggle-muted
             :sub-label           (when muted
                                    (str (i18n/label :t/muted-until)
                                         " "
                                         (format-mute-till muted-till)))
             :on-press            (if muted?
                                    #(unmute-channel-action id)
                                    #(mute-channel-action id chat-type))
             :label               (i18n/label (if muted
                                                :t/unmute-channel
                                                :t/mute-channel))}
      (not muted?) (assoc :right-icon :i/chevron-right))))

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
  [{:keys [locked? chat-id]} inside-chat?]
  (let [{:keys [muted muted-till chat-type]} (rf/sub [:chat-by-id chat-id])]
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
         (action-toggle-muted chat-id muted muted-till chat-type)
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
         (action-toggle-muted chat-id muted muted-till chat-type)
         (action-notification-settings)
         (action-fetch-messages)
         (action-invite-people)
         (action-qr-code)
         (action-share)]]]

      :else nil)))
