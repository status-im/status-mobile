(ns status-im.contexts.communities.actions.chat.view
  (:require
    [quo.core :as quo]
    [status-im.common.mute-drawer.view :as mute-drawer]
    [status-im.common.muting.helpers :refer [format-mute-till]]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.config :as config]
    [status-im.contexts.chat.actions.view :as chat-actions]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch event))

(defn- mute-channel-action
  [chat-id chat-type muted?]
  (hide-sheet-and-dispatch [:show-bottom-sheet
                            {:content (fn []
                                        [mute-drawer/mute-drawer
                                         {:id                  chat-id
                                          :community?          false
                                          :muted?              (not muted?)
                                          :chat-type           chat-type
                                          :accessibility-label :mute-community-title}])}]))

(defn- unmute-channel-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/mute chat-id false 0]))

(defn- action-view-members-and-details
  [community-id chat-id]
  {:icon                :i/members
   :accessibility-label :chat-view-members-and-details
   :label               (i18n/label :t/view-channel-members-and-details)
   :on-press            #(rf/dispatch [:navigate-to :screen/chat.view-channel-members-and-details
                                       {:community-id community-id :chat-id chat-id}])})

(defn- action-token-requirements
  []
  (when config/show-not-implemented-features?
    {:icon                :i/token
     :right-icon          :i/chevron-right
     :accessibility-label :chat-view-token-requirements
     :on-press            not-implemented/alert
     :label               (i18n/label :t/view-token-gating)}))

(defn- action-mark-as-read
  []
  (when config/show-not-implemented-features?
    {:icon                :i/mark-as-read
     :accessibility-label :chat-mark-as-read
     :on-press            not-implemented/alert
     :label               (i18n/label :t/mark-as-read)}))

(defn- action-toggle-muted
  [id muted? muted-till chat-type]
  (let [muted       (and muted? (some? muted-till))
        time-string (fn [mute-title mute-duration]
                      (i18n/label mute-title {:duration mute-duration}))]
    (cond-> {:icon                :i/muted
             :accessibility-label :chat-toggle-muted
             :sub-label           (when (and muted? (some? muted-till))
                                    (time-string :t/muted-until (format-mute-till muted-till)))
             :on-press            (if muted?
                                    #(unmute-channel-action id)
                                    #(mute-channel-action id chat-type muted?))
             :label               (i18n/label (if muted
                                                :t/unmute-channel
                                                :t/mute-channel))}
      (not muted?) (assoc :right-icon :i/chevron-right))))

(defn- action-notification-settings
  []
  (when config/show-not-implemented-features?
    {:icon                :i/notifications
     :right-icon          :i/chevron-right
     :accessibility-label :chat-notification-settings
     :on-press            not-implemented/alert
     :label               (i18n/label :t/notification-settings)
     :sub-label           (i18n/label :t/only-mentions)}))

(defn- action-pinned-messages
  [chat-id]
  {:icon                :i/pin
   :right-icon          :i/chevron-right
   :accessibility-label :chat-pinned-messages
   :on-press            (fn []
                          (rf/dispatch [:pin-message/load-pin-messages chat-id])
                          (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id]))
   :label               (i18n/label :t/pinned-messages)})


(defn- action-invite-people
  []
  (when config/show-not-implemented-features?
    {:icon                :i/add-user
     :accessibility-label :chat-invite-people
     :on-press            not-implemented/alert
     :label               (i18n/label :t/invite-people-from-contacts)}))

(defn- action-qr-code
  [chat-id]
  {:icon                :i/qr-code
   :accessibility-label :chat-show-qr-code
   :on-press            #(rf/dispatch [:communities/share-community-channel-url-qr-code chat-id])
   :label               (i18n/label :t/show-qr)})

(defn- action-share
  [chat-id]
  {:icon                :i/share
   :accessibility-label :chat-share
   :on-press            #(rf/dispatch [:communities/share-community-channel-url-with-data chat-id])
   :label               (i18n/label :t/share-channel)})

(defn actions
  [{:keys [locked? chat-id community-id]} inside-chat? hide-view-members?]
  (let [{:keys [muted muted-till chat-type]} (rf/sub [:chats/chat-by-id chat-id])
        {:keys [token-gated?]}               (rf/sub [:chats/community-chat-by-id community-id chat-id])]
    (cond
      locked?
      [quo/action-drawer
       [[(action-invite-people)
         (when token-gated?
           (action-token-requirements))
         (action-qr-code chat-id)
         (action-share chat-id)]]]

      (and (not inside-chat?) (not locked?))
      [quo/action-drawer
       [[(when-not hide-view-members? (action-view-members-and-details community-id chat-id))
         (action-mark-as-read)
         (action-toggle-muted chat-id muted muted-till chat-type)
         (action-notification-settings)
         (action-pinned-messages chat-id)
         (action-invite-people)
         (action-qr-code chat-id)
         (action-share chat-id)]]]

      (and inside-chat? (not locked?))
      [quo/action-drawer
       [[(action-view-members-and-details community-id chat-id)
         (when token-gated?
           (action-token-requirements))
         (action-mark-as-read)
         (action-toggle-muted chat-id muted muted-till chat-type)
         (action-notification-settings)
         (when config/fetch-messages-enabled?
           (chat-actions/fetch-messages chat-id))
         (action-invite-people)
         (action-qr-code chat-id)
         (action-share chat-id)]]]

      :else nil)))
