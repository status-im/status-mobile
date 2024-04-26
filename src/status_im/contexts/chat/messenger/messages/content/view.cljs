(ns status-im.contexts.chat.messenger.messages.content.view
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.screens.chat.message.legacy-view :as old-message]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [reagent.core :as reagent]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.composer.reply.view :as reply]
    [status-im.contexts.chat.messenger.messages.avatar.view :as avatar]
    [status-im.contexts.chat.messenger.messages.content.album.view :as album]
    [status-im.contexts.chat.messenger.messages.content.audio.view :as audio]
    [status-im.contexts.chat.messenger.messages.content.deleted.view :as content.deleted]
    [status-im.contexts.chat.messenger.messages.content.emoji-message.view :as emoji-message]
    [status-im.contexts.chat.messenger.messages.content.image.view :as image]
    [status-im.contexts.chat.messenger.messages.content.pin.view :as pin]
    [status-im.contexts.chat.messenger.messages.content.reactions.view :as reactions]
    [status-im.contexts.chat.messenger.messages.content.status.view :as status]
    [status-im.contexts.chat.messenger.messages.content.sticker-message.view :as sticker-message]
    [status-im.contexts.chat.messenger.messages.content.style :as style]
    [status-im.contexts.chat.messenger.messages.content.system.text.view :as system.text]
    [status-im.contexts.chat.messenger.messages.content.text.view :as content.text]
    [status-im.contexts.chat.messenger.messages.content.unknown.view :as content.unknown]
    [status-im.contexts.chat.messenger.messages.drawers.view :as drawers]
    [utils.address :as address]
    [utils.datetime :as datetime]
    [utils.re-frame :as rf]))

(def delivery-state-showing-time-ms 3000)

(defn avatar-container
  [{:keys [content last-in-group? pinned-by quoted-message from]} show-reactions?
   in-reaction-and-action-menu? show-user-info? in-pinned-view?]
  (if (or (and (seq (:response-to content))
               quoted-message)
          last-in-group?
          show-user-info?
          pinned-by
          (not show-reactions?)
          in-reaction-and-action-menu?)
    [avatar/avatar
     {:public-key from
      :size       :small
      :hide-ring? (or in-pinned-view? in-reaction-and-action-menu?)}]
    [rn/view {:padding-top 4 :width 32}]))

(defn author
  [{:keys [content
           compressed-key
           last-in-group?
           pinned-by
           quoted-message
           from
           timestamp]}
   show-reactions?
   in-reaction-and-action-menu?
   show-user-info?]
  (when (or (and (seq (:response-to content)) quoted-message)
            last-in-group?
            pinned-by
            show-user-info?
            (not show-reactions?)
            in-reaction-and-action-menu?)
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
  (let [[primary-name _]       (rf/sub [:contacts/contact-two-names-by-identity chat-id])
        {:keys [images]
         contact-customization-color
         :customization-color} (rf/sub [:contacts/contact-by-address chat-id])
        photo-path             (when (seq images) (rf/sub [:chats/photo-path chat-id]))
        public-key             (rf/sub [:profile/public-key])]
    [quo/system-message
     {:type                type
      :timestamp           timestamp-str
      :display-name        primary-name
      :customization-color contact-customization-color
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

(defn bridge-message-content
  [{:keys [bridge-message timestamp]}]
  (let [{:keys [user-avatar user-name
                bridge-name content]} bridge-message
        user-name                     (when (string? user-name)
                                        (-> user-name
                                            (string/replace "<b>" "")
                                            (string/replace "</b>" "")))]
    [rn/view
     {:style {:flex-direction     :row
              :padding-horizontal 12
              :padding-top        4}}
     [fast-image/fast-image
      {:source {:uri user-avatar}
       :style  {:width         32
                :margin-top    4
                :border-radius 16
                :height        32}}]
     [rn/view {:margin-left 8 :flex 1}
      [quo/author
       {:primary-name   (str user-name)
        :short-chat-key (str "Bridged from " bridge-name)
        :time-str       (datetime/timestamp->time timestamp)}]
      [quo/text
       {:size  :paragraph-1
        :style {:line-height 22.75}}
       content]]]))

(declare on-long-press)

(defn user-message-content
  []
  (let [show-delivery-state? (reagent/atom false)]
    (fn [{:keys [message-data context keyboard-shown? show-reactions? in-reaction-and-action-menu?
                 show-user-info? preview?]}]
      (let [theme                                       (quo.theme/use-theme)
            {:keys [content-type quoted-message content outgoing outgoing-status pinned-by pinned
                    last-in-group? message-id chat-id]} message-data
            {:keys [disable-message-long-press?]}       context
            first-image                                 (first (:album message-data))
            outgoing-status                             (if (= content-type
                                                               constants/content-type-album)
                                                          (:outgoing-status first-image)
                                                          outgoing-status)
            outgoing                                    (if (= content-type
                                                               constants/content-type-album)
                                                          (:outgoing first-image)
                                                          outgoing)
            context                                     (assoc context
                                                               :on-long-press
                                                               #(on-long-press message-data
                                                                               context
                                                                               keyboard-shown?))
            response-to                                 (:response-to content)
            height                                      (rf/sub [:dimensions/window-height])
            {window-width :width
             window-scale :scale}                       (rn/get-window)
            message-container-data                      {:window-width           window-width
                                                         :padding-right          20
                                                         :padding-left           20
                                                         :avatar-container-width 32
                                                         :message-margin-left    8}
            reactions                                   (rf/sub [:chats/message-reactions message-id
                                                                 chat-id])
            six-reactions?                              (-> reactions
                                                            count
                                                            (= 6))]
        [rn/touchable-highlight
         {:accessibility-label (if (and outgoing (= outgoing-status :sending))
                                 :message-sending
                                 :message-sent)
          :underlay-color      (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)
          :style               (style/user-message-content
                                {:first-in-group? (:first-in-group? message-data)
                                 :outgoing        outgoing
                                 :outgoing-status outgoing-status
                                 :small-screen?   rn/small-screen?
                                 :window-scale    window-scale
                                 :six-reactions?  six-reactions?
                                 :preview?        preview?})
          :on-press            (fn []
                                 (if (and platform/ios? keyboard-shown?)
                                   (do
                                     (rf/dispatch [:chat.ui/set-input-focused false])
                                     (rn/dismiss-keyboard!))
                                   (when (and outgoing
                                              (not= outgoing-status :sending)
                                              (not @show-delivery-state?))
                                     (reset! show-delivery-state? true)
                                     (js/setTimeout #(reset! show-delivery-state? false)
                                                    delivery-state-showing-time-ms))))
          :on-long-press       (when-not disable-message-long-press?
                                 #(on-long-press message-data context keyboard-shown?))}
         [:<>
          (when pinned-by
            [pin/pinned-by-view pinned-by])
          (when (and (seq response-to) quoted-message)
            [reply/quoted-message quoted-message])
          [rn/view
           {:style {:padding-horizontal 4
                    :flex-direction     :row}}
           [avatar-container message-data show-reactions? in-reaction-and-action-menu? show-user-info?
            (:in-pinned-view? context)]
           (into
            (if show-reactions?
              [rn/view]
              [gesture/scroll-view])
            [{:style {:margin-left 8
                      :flex        1
                      :gap         1
                      :max-height  (when-not show-reactions?
                                     (* 0.4 height))}}
             [author message-data show-reactions? in-reaction-and-action-menu? show-user-info?]
             (condp = content-type
               constants/content-type-text
               [content.text/text-content message-data context]

               constants/content-type-emoji
               [emoji-message/view
                {:content         content
                 :last-in-group?  last-in-group?
                 :pinned          pinned
                 :in-pinned-view? (:in-pinned-view? context)}]

               constants/content-type-sticker
               [sticker-message/view {:url (-> message-data :content :sticker :url)}]

               constants/content-type-audio
               [audio/audio-message message-data context]

               constants/content-type-image
               [image/image-message 0 message-data context 0 message-container-data]

               constants/content-type-album
               [album/album-message message-data context on-long-press message-container-data]

               constants/content-type-gap
               [rn/view]

               [not-implemented/not-implemented
                [content.unknown/unknown-content message-data]])

             (when @show-delivery-state?
               [status/status outgoing-status])])]
          (when show-reactions?
            [reactions/message-reactions-row (assoc message-data :preview? preview?)
             [rn/view {:pointer-events :none}
              [user-message-content
               {:theme                        theme
                :message-data                 message-data
                :context                      context
                :in-reaction-and-action-menu? true
                :keyboard-shown?              keyboard-shown?
                :preview?                     true
                :show-reactions?              true}]]])]]))))

(defn on-long-press
  [{:keys [deleted? deleted-for-me?] :as message-data} context keyboard-shown?]
  (rf/dispatch [:dismiss-keyboard])
  (rf/dispatch
   [:show-bottom-sheet
    {:content (drawers/reactions-and-actions message-data context)
     :border-radius 16
     :selected-item
     (if (or deleted? deleted-for-me?)
       (fn [] [content.deleted/deleted-message message-data])
       (fn []
         [rn/view
          {:pointer-events :none
           :style          style/drawer-message-container}
          [user-message-content
           {:message-data                 message-data
            :context                      context
            :keyboard-shown?              keyboard-shown?
            :in-reaction-and-action-menu? true
            :show-user-info?              false
            :show-reactions?              true}]]))}]))

(defn check-if-system-message?
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
  (let [system-message? (check-if-system-message? content-type)]
    [rn/view
     {:style               (style/message-container
                            {:in-pinned-view? in-pinned-view?
                             :pinned-by       pinned-by
                             :mentioned       mentioned
                             :last-in-group?  last-in-group?
                             :system-message? system-message?})
      :accessibility-label :chat-item}
     (cond
       system-message?
       [system-message-content message-data]

       (or deleted? deleted-for-me?)
       [content.deleted/deleted-message
        (assoc message-data
               :on-long-press
               #(on-long-press message-data
                               context
                               keyboard-shown?))
        context]


       (= content-type constants/content-type-bridge-message)
       [bridge-message-content message-data]

       :else
       [user-message-content
        {:message-data    message-data
         :context         context
         :keyboard-shown? keyboard-shown?
         :show-reactions? true}])]))
