(ns status-im2.contexts.chat.messages.content.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.chat.messages.content.style :as style]
            [status-im2.contexts.chat.messages.content.pin.view :as pin]
            [status-im2.common.constants :as constants]
            [status-im2.contexts.chat.messages.content.unknown.view :as content.unknown]
            [status-im2.contexts.chat.messages.content.text.view :as content.text]
            [status-im2.contexts.chat.messages.content.deleted.view :as content.deleted]
            [status-im2.contexts.chat.messages.drawers.view :as drawers]
            [status-im2.contexts.chat.messages.content.reactions.view :as reactions]
            [status-im2.contexts.chat.messages.content.status.view :as status]
            [status-im2.contexts.chat.messages.content.system.text.view :as system.text]
            [quo2.core :as quo]
            [utils.re-frame :as rf]

            ;; TODO (flexsurfer) move to status-im2
            [status-im.utils.datetime :as time]
            [status-im.ui2.screens.chat.messages.message :as old-message]))

(defn avatar [{:keys [response-to last-in-group? pinned quoted-message from]}]
  [rn/view {:padding-top 2 :width 32}
   (when (or (and (seq response-to) quoted-message) last-in-group? pinned)
     (let [display-name (first (rf/sub [:contacts/contact-two-names-by-identity from]))
           contact (rf/sub [:contacts/contact-by-address from])
           photo-path (when-not (empty? (:images contact)) (rf/sub [:chats/photo-path from]))
           online? (rf/sub [:visibility-status-updates/online? from])]
       [quo/user-avatar {:full-name         display-name
                         :profile-picture   photo-path
                         :status-indicator? true
                         :online?           online?
                         :size              :small
                         :ring?             false}]))])

(defn author [{:keys [response-to last-in-group? pinned quoted-message from timestamp]}]
  (when (or (and (seq response-to) quoted-message) last-in-group? pinned)
    (let [display-name (first (rf/sub [:contacts/contact-two-names-by-identity from]))
          {:keys [ens-verified added?]} (rf/sub [:contacts/contact-by-address from])]
      [quo/author
       {:profile-name   display-name
        :chat-key       from
        :time-str       (time/timestamp->time timestamp)
        :contact?       added?
        :verified?      ens-verified
        ;; TODO not implemented yet
        :nickname       nil
        :ens-name       nil
        :untrustworthy? nil}])))

;;;; SYSTEM MESSAGES
(defn system-message-content [{:keys [content-type quoted-message] :as message-data}]
  (if quoted-message
    [pin/system-message quoted-message] ;;TODO (flexsurfer) why we detect pin-message with quoted-message param?
    (case content-type
      ;; TEXT
      constants/content-type-system-text [system.text/text-content message-data]
      ;; COMMUNITY TODO (flexsurfer) refactor and move to status-im2
      constants/content-type-community [old-message/community message-data]
      ;; CONTACT REQUEST  TODO (flexsurfer) refactor and move to status-im2
      constants/content-type-contact-request [old-message/system-contact-request message-data])))

;;;; USER MESSAGES
(defn user-message-content [{:keys [content-type] :as message-data}]
  [rn/touchable-highlight {:underlay-color (colors/theme-colors colors/neutral-5 colors/neutral-90)
                           :style          {:border-radius 16}
                           :on-press       #()
                           :on-long-press  #(rf/dispatch [:bottom-sheet/show-sheet
                                                          {:content (drawers/reactions-and-actions message-data)}])}
   [rn/view {:padding-horizontal 12 :padding-vertical 8 :flex-direction :row}
    ;; AVATAR
    [avatar message-data]
    [rn/view {:margin-left 8}
     ;; AUTHOR NAME
     [author message-data]
     (case content-type
       ;; TEXT TODO (flexsurfer) refactor and move to status-im2
       constants/content-type-text [content.text/text-content message-data]
       ;; EMOJI TODO (flexsurfer) refactor and move to status-im2
       constants/content-type-emoji [old-message/emoji message-data]
       ;; STICKER TODO (flexsurfer) refactor and move to status-im2
       constants/content-type-sticker [old-message/sticker message-data]
       ;; IMAGE TODO (flexsurfer) refactor and move to status-im2
       constants/content-type-image [old-message/message-content-image message-data]
       ;; AUDIO TODO (flexsurfer) refactor and move to status-im2
       constants/content-type-audio [old-message/audio message-data]
       ;; UNKNOWN
       [content.unknown/unknown-content message-data])
     [status/status message-data]]]])

(defn message-with-reactions [{:keys [pinned pinned-by mentioned in-pinned-view? content-type
                                      last-in-group? deleted? deleted-for-me? message-id]
                               :as   message-data}
                              {:keys [chat-id]}]
  (if (or deleted? deleted-for-me?)
    ;; DELETED
    [content.deleted/deleted-message message-data]
    [rn/view {:style (style/message-container in-pinned-view? pinned mentioned last-in-group?)}
     ;; PIN
     (when pinned
       [pin/pinned-by-view pinned-by])
     ;; MESSAGE
     (if (#{constants/content-type-system-text constants/content-type-community
            constants/content-type-contact-request} content-type)
       [system-message-content message-data]
       [user-message-content message-data])
     ;; REACTIONS ROW
     [reactions/message-reactions-row chat-id message-id]]))
