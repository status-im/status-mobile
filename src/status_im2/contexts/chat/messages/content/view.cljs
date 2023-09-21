(ns status-im2.contexts.chat.messages.content.view
  (:require
    [react-native.core :as rn]
    [quo2.foundations.colors :as colors]
    [react-native.platform :as platform]
    [status-im2.contexts.chat.messages.content.style :as style]
    [status-im2.contexts.chat.messages.content.pin.view :as pin]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.messages.content.deleted.view :as content.deleted]
    [status-im2.contexts.chat.messages.content.unknown.view :as content.unknown]
    [status-im2.contexts.chat.messages.content.text.view :as content.text]
    [status-im2.contexts.chat.messages.drawers.view :as drawers]
    [status-im2.contexts.chat.messages.content.reactions.view :as reactions]
    [status-im2.contexts.chat.messages.content.status.view :as status]
    [status-im2.contexts.chat.messages.content.system.text.view :as system.text]
    [status-im2.contexts.chat.messages.content.album.view :as album]
    [status-im2.contexts.chat.messages.avatar.view :as avatar]
    [status-im2.contexts.chat.messages.content.image.view :as image]
    [status-im2.contexts.chat.messages.content.audio.view :as audio]
    [quo2.core :as quo]
    [utils.re-frame :as rf]
    [status-im.ui.screens.chat.message.legacy-view :as old-message]
    [status-im2.contexts.chat.composer.reply.view :as reply]
    [status-im2.common.not-implemented :as not-implemented]
    [utils.datetime :as datetime]
    [reagent.core :as reagent]
    [utils.address :as address]
    [react-native.gesture :as gesture]
    [quo2.theme :as quo.theme]))

(def delivery-state-showing-time-ms 3000)

(defn avatar-container
  [{:keys [content last-in-group? pinned-by quoted-message from]} show-reactions? show-user-info?]
  (if (or (and (seq (:response-to content))
               quoted-message)
          last-in-group?
          pinned-by
          (not show-reactions?)
          show-user-info?)
    [avatar/avatar from :small]
    [rn/view {:padding-top 2 :width 32}]))

(defn author
  [{:keys [response-to
           compressed-key
           last-in-group?
           pinned-by
           quoted-message
           from
           timestamp]}
   show-reactions?
   show-user-info?]
  (when (or (and (seq response-to) quoted-message)
            last-in-group?
            pinned-by
            (not show-reactions?)
            show-user-info?)
    (let [[primary-name secondary-name] (rf/sub [:contacts/contact-two-names-by-identity from])
          {:keys [ens-verified added?]} (rf/sub [:contacts/contact-by-address from])]
      [quo/author
       {:primary-name   primary-name
        :secondary-name secondary-name
        :short-chat-key (address/get-shortened-compressed-key (or compressed-key from))
        :time-str       (datetime/timestamp->time timestamp)
        :contact?       added?
        :verified?      ens-verified}])))

(defn system-message-contact-request
  [{:keys [chat-id timestamp-str from]} type]
  (let [display-name         (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
        contact              (rf/sub [:contacts/contact-by-address chat-id])
        photo-path           (when (seq (:images contact)) (rf/sub [:chats/photo-path chat-id]))
        customization-color  (rf/sub [:profile/customization-color])
        {:keys [public-key]} (rf/sub [:profile/profile])]
    [quo/system-message
     {:type                type
      :timestamp           timestamp-str
      :display-name        display-name
      :customization-color customization-color
      :photo-path          photo-path
      :incoming?           (not= public-key from)}]))

(defn system-message-content
  [{:keys [content-type quoted-message] :as message-data}]
  (if quoted-message
    [pin/pinned-message message-data]
    (condp = content-type

      constants/content-type-system-text
      [system.text/text-content message-data]

      constants/content-type-system-pinned-message
      [system.text/text-content message-data]

      constants/content-type-community
      [not-implemented/not-implemented
       [old-message/community message-data]]

      constants/content-type-system-message-mutual-event-accepted
      [system-message-contact-request message-data :added]

      constants/content-type-system-message-mutual-event-removed
      [system-message-contact-request message-data :removed]

      constants/content-type-system-message-mutual-event-sent
      [system-message-contact-request message-data :contact-request])))

(declare on-long-press)

(defn- user-message-content-internal
  []
  (let [show-delivery-state? (reagent/atom false)]
    (fn [{:keys [message-data context keyboard-shown? show-reactions? show-user-info? theme]}]
      (let [{:keys [content-type quoted-message content
                    outgoing outgoing-status pinned-by]} message-data
            first-image                                  (first (:album message-data))
            outgoing-status                              (if (= content-type
                                                                constants/content-type-album)
                                                           (:outgoing-status first-image)
                                                           outgoing-status)
            outgoing                                     (if (= content-type
                                                                constants/content-type-album)
                                                           (:outgoing first-image)
                                                           outgoing)
            context                                      (assoc context
                                                                :on-long-press
                                                                #(on-long-press message-data
                                                                                context
                                                                                keyboard-shown?))
            response-to                                  (:response-to content)
            height                                       (rf/sub [:dimensions/window-height])]
        [rn/touchable-highlight
         {:accessibility-label (if (and outgoing (= outgoing-status :sending))
                                 :message-sending
                                 :message-sent)
          :underlay-color      (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)
          :style               (style/user-message-content
                                {:first-in-group? (:first-in-group? message-data)
                                 :outgoing        outgoing
                                 :outgoing-status outgoing-status})
          :on-press            (fn []
                                 (if (and platform/ios? keyboard-shown?)
                                   (rn/dismiss-keyboard!)
                                   (when (and outgoing
                                              (not= outgoing-status :sending)
                                              (not @show-delivery-state?))
                                     (reset! show-delivery-state? true)
                                     (js/setTimeout #(reset! show-delivery-state? false)
                                                    delivery-state-showing-time-ms))))
          :on-long-press       #(on-long-press message-data context keyboard-shown?)}
         [:<>
          (when pinned-by
            [pin/pinned-by-view pinned-by])
          (when (and (seq response-to) quoted-message)
            [reply/quoted-message quoted-message])
          [rn/view
           {:style {:padding-horizontal 4
                    :flex-direction     :row}}
           [avatar-container message-data show-reactions? show-user-info?]
           (into
            (if show-reactions?
              [rn/view]
              [gesture/scroll-view])
            [{:style {:margin-left 8
                      :flex        1
                      :max-height  (when-not show-reactions?
                                     (* 0.4 height))}}
             [author message-data show-reactions? show-user-info?]
             (case content-type

               constants/content-type-text
               [content.text/text-content message-data context]

               constants/content-type-emoji
               [not-implemented/not-implemented [old-message/emoji message-data]]

               constants/content-type-sticker
               [not-implemented/not-implemented [old-message/sticker message-data]]

               constants/content-type-audio
               [audio/audio-message message-data context]

               constants/content-type-image
               [image/image-message 0 message-data context]

               constants/content-type-album
               [album/album-message message-data context on-long-press]

               [not-implemented/not-implemented
                [content.unknown/unknown-content message-data]])

             (when @show-delivery-state?
               [status/status outgoing-status])])]
          (when show-reactions?
            [reactions/message-reactions-row message-data
             [rn/view {:pointer-events :none}
              [user-message-content-internal
               {:theme           theme
                :message-data    message-data
                :context         context
                :keyboard-shown? keyboard-shown?
                :show-reactions? false}]]])]]))))

(def user-message-content (quo.theme/with-theme user-message-content-internal))

(defn on-long-press
  [message-data context keyboard-shown?]
  (rf/dispatch [:dismiss-keyboard])
  (rf/dispatch [:show-bottom-sheet
                {:content       (drawers/reactions-and-actions message-data context)
                 :selected-item (fn []
                                  [rn/view {:pointer-events :none}
                                   [user-message-content
                                    {:message-data    message-data
                                     :context         context
                                     :keyboard-shown? keyboard-shown?
                                     :show-reactions? true
                                     :show-user-info? true}]])}]))

(defn system-message?
  [content-type]
  (#{constants/content-type-system-text
     constants/content-type-community
     constants/content-type-system-message-mutual-event-accepted
     constants/content-type-system-message-mutual-event-removed
     constants/content-type-system-message-mutual-event-sent
     constants/content-type-system-pinned-message}
   content-type))

(defn message
  [{:keys [pinned-by mentioned content-type last-in-group? deleted? deleted-for-me?]
    :as   message-data} {:keys [in-pinned-view?] :as context} keyboard-shown?]
  [rn/view
   {:style               (style/message-container in-pinned-view? pinned-by mentioned last-in-group?)
    :accessibility-label :chat-item}
   (if (or (system-message? content-type) deleted? deleted-for-me?)
     (if (or deleted? deleted-for-me?)
       [content.deleted/deleted-message message-data]
       [system-message-content message-data])
     [user-message-content
      {:message-data    message-data
       :context         context
       :keyboard-shown? keyboard-shown?
       :show-reactions? true}])])
