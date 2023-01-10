(ns status-im2.contexts.chat.messages.content.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.chat.messages.content.style :as style]
            [status-im2.contexts.chat.messages.content.pin.view :as pin]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.messages.content.unknown.view :as content.unknown]
            [status-im2.contexts.chat.messages.content.text.view :as content.text]
            [status-im2.contexts.chat.messages.drawers.view :as drawers]
            [status-im2.contexts.chat.messages.content.reactions.view :as reactions]
            [status-im2.contexts.chat.messages.content.status.view :as status]
            [status-im2.contexts.chat.messages.content.system.text.view :as system.text]
            [status-im2.contexts.chat.messages.content.album.view :as album]
            [status-im2.contexts.chat.messages.content.image.view :as image]
            [quo2.core :as quo]
            [utils.re-frame :as rf]
            [status-im.ui2.screens.chat.messages.message :as old-message]
            [status-im2.common.not-implemented :as not-implemented]
            [utils.datetime :as datetime]))

(defn avatar
  [{:keys [response-to last-in-group? pinned quoted-message from]}]
  [rn/touchable-without-feedback {:on-press #(rf/dispatch [:chat.ui/show-profile from])}
   [rn/view {:padding-top 2 :width 32}
    (when (or (and (seq response-to) quoted-message) last-in-group? pinned)
      (let [display-name (first (rf/sub [:contacts/contact-two-names-by-identity from]))
            contact      (rf/sub [:contacts/contact-by-address from])
            photo-path   (when-not (empty? (:images contact)) (rf/sub [:chats/photo-path from]))
            online?      (rf/sub [:visibility-status-updates/online? from])]
        [quo/user-avatar
         {:full-name         display-name
          :profile-picture   photo-path
          :status-indicator? true
          :online?           online?
          :size              :small
          :ring?             false}]))]])

(defn author
  [{:keys [response-to last-in-group? pinned quoted-message from timestamp]}]
  (when (or (and (seq response-to) quoted-message) last-in-group? pinned)
    (let [display-name                  (first (rf/sub [:contacts/contact-two-names-by-identity from]))
          {:keys [ens-verified added?]} (rf/sub [:contacts/contact-by-address from])]
      [quo/author
       {:profile-name display-name
        :chat-key     from
        :time-str     (datetime/timestamp->time timestamp)
        :contact?     added?
        :verified?    ens-verified}])))

(defn system-message-content
  [{:keys [content-type quoted-message] :as message-data}]
  (if quoted-message
    [not-implemented/not-implemented [pin/system-message message-data]]
    (case content-type

      constants/content-type-system-text     [not-implemented/not-implemented
                                              [system.text/text-content message-data]]

      constants/content-type-community       [not-implemented/not-implemented
                                              [old-message/community message-data]]

      constants/content-type-contact-request [not-implemented/not-implemented
                                              [old-message/system-contact-request message-data]])))

(defn user-message-content
  [{:keys [content-type quoted-message content] :as message-data}
   {:keys [chat-id] :as context}]
  (let [response-to (:response-to content)]
    [rn/touchable-highlight
     {:underlay-color (colors/theme-colors colors/neutral-5 colors/neutral-90)
      :style          {:border-radius 16}
      :on-press       #()
      :on-long-press  #(rf/dispatch [:bottom-sheet/show-sheet
                                     {:content (drawers/reactions-and-actions message-data context)}])}
     [rn/view {:padding-vertical 8}
      (when (and (seq response-to) quoted-message)
        [old-message/quoted-message {:message-id response-to :chat-id chat-id} quoted-message])
      [rn/view {:padding-horizontal 12 :flex-direction :row}
       [avatar message-data]
       [rn/view {:margin-left 8 :flex 1}
        [author message-data]
        (case content-type

          constants/content-type-text    [not-implemented/not-implemented
                                          [content.text/text-content message-data]]

          constants/content-type-emoji   [not-implemented/not-implemented
                                          [old-message/emoji message-data]]

          constants/content-type-sticker [not-implemented/not-implemented
                                          [old-message/sticker message-data]]

          constants/content-type-image   [image/image-message message-data]

          constants/content-type-audio   [not-implemented/not-implemented
                                          [old-message/audio message-data]]

          constants/content-type-album   [album/album-message message-data]

          [not-implemented/not-implemented [content.unknown/unknown-content message-data]])
        [status/status message-data]]]]]))

(defn message-with-reactions
  [{:keys [pinned pinned-by mentioned in-pinned-view? content-type
           last-in-group? message-id]
    :as   message-data}
   {:keys [chat-id] :as context}]
  [rn/view
   {:style               (style/message-container in-pinned-view? pinned mentioned last-in-group?)
    :accessibility-label :chat-item}
   (when pinned
     [pin/pinned-by-view pinned-by])
   (if (#{constants/content-type-system-text constants/content-type-community
          constants/content-type-contact-request}
        content-type)
     [system-message-content message-data]
     [user-message-content message-data context])
   [reactions/message-reactions-row chat-id message-id]])
