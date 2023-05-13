(ns status-im2.contexts.communities.menus.channel-options.view
  (:require [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [quo2.core :as quo]
            [status-im2.common.mute-chat-drawer.view :as mute-chat-drawer]
            [utils.datetime :as datetime]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch event))

(defn mute-channel-action
  [chat-id]
  (hide-sheet-and-dispatch [:show-bottom-sheet
                            {:content (fn []
                                        [mute-chat-drawer/mute-chat-drawer chat-id
                                         :mute-chat-for-duration])}]))

(defn unmute-channel-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/mute chat-id false 0]))

(defn view-members-option
  [_id]
  {:icon                :i/members
   :accessibility-label :view-members
   :label               (i18n/label :t/view-channel-members-and-details)})

(defn view-token-gating-option
  [id]
  {:icon                :i/token
   :right-icon          :i/chevron-right
   :chevron?            true
   :accessibility-label :view-token-gating
   :on-press            #(js/alert (str "implement action" id))
   :label               (i18n/label :t/view-token-gating)})

(defn mark-as-read-option
  [_id]
  {:icon                :i/up-to-date
   :accessibility-label :mark-as-read
   :label               (i18n/label :t/mark-as-read)})

(defn mute-channel-option
  [id muted? muted-till]
  {:icon                (if muted? :i/muted :i/activity-center)
   :accessibility-label (if muted? :unmute-channel :mute-channel)
   :label               (i18n/label (if muted? :t/unmute-channel :t/mute-channel))
   :sub-label           (when (and muted? (some? muted-till))
                          (str (i18n/label :t/muted-until) " " (datetime/format-mute-till muted-till)))
   :on-press            (if muted?
                          #(unmute-channel-action id)
                          #(mute-channel-action id))
   :right-icon          :i/chevron-right
   :chevron?            true})

(defn fetch-messages-option
  []
  {:icon                :i/save
   :label               (i18n/label :t/fetch-messages)
   :on-press            #(js/alert "TODO: to be implemented, requires design input")
   :danger?             false
   :accessibility-label :fetch-messages
   :sub-label           nil
   :right-icon          :i/chevron-right
   :chevron?            true})

(defn invite-contacts-option
  [id]
  {:icon                :i/add-user
   :accessibility-label :invite-people-from-contacts
   :label               (i18n/label :t/invite-people-from-contacts)
   :on-press            #(hide-sheet-and-dispatch [:communities/invite-people-pressed id])})

(defn show-qr-option
  [id]
  {:icon                :i/qr-code
   :accessibility-label :show-qr
   :on-press            #(js/alert (str "implement action" id))
   :label               (i18n/label :t/show-qr)})

(defn share-channel-option
  [_id]
  {:icon                :i/share
   :accessibility-label :share-channel
   :label               (i18n/label :t/share-channel)})

(defn notifications-option
  []
  {:icon                :i/notifications
   :label               (i18n/label :t/notifications)
   :on-press            #(js/alert "TODO: to be implemented, requires design input")
   :danger?             false
   :sub-label           (i18n/label :t/all-messages)
   :accessibility-label :manage-notifications
   :right-icon          :i/chevron-right
   :chevron?            true
   :add-divider?        false})

(defn channel-options-bottom-sheet
  [community-id id]
  (let [{:keys [token-gated?]}     (rf/sub [:communities/community id])
        {:keys [muted muted-till]} (rf/sub [:chat-by-id (str community-id id)])]
    [quo/action-drawer
     [[(view-members-option id)
       (when token-gated? (view-token-gating-option id))
       (mark-as-read-option id)
       (mute-channel-option (str community-id id) muted muted-till)
       (notifications-option)
       (fetch-messages-option)
       (invite-contacts-option id)
       (show-qr-option id)
       (share-channel-option id)]]]))
