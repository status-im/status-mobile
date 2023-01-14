(ns status-im2.contexts.activity-center.notification.reply.view
  (:require [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im.ui2.screens.chat.messages.message :as old-message]
            [status-im2.common.constants :as constants]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [status-im2.contexts.activity-center.notification.reply.style :as style]
            [utils.datetime :as datetime]
            [utils.re-frame :as rf]))

(def tag-params
  {:size           :small
   :override-theme :dark
   :color          colors/primary-50
   :style          style/tag
   :text-style     style/tag-text})

;; NOTE: Replies support text, image and stickers only.
(defn get-message-content
  [{:keys [content-type] :as message}]
  (case content-type
    constants/content-type-text    (get-in message [:content :text])

    constants/content-type-image   [old-message/message-content-image message]

    constants/content-type-sticker [old-message/sticker message]))

(defn view
  [{:keys [author chat-name chat-id message read timestamp]}]
  (let [chat                    (rf/sub [:chats/chat chat-id])
        community-id            (:community-id chat)
        is-chat-from-community? (not (nil? community-id))
        community               (rf/sub [:communities/community community-id])
        community-name          (:name community)
        community-image         (get-in community [:images :thumbnail :uri])]
    [rn/touchable-opacity
     {:on-press (fn []
                  (rf/dispatch [:hide-popover])
                  (rf/dispatch [:chat.ui/navigate-to-chat-nav2 chat-id]))}
     [quo/activity-log
      {:title     (i18n/label :t/message-reply)
       :icon      :i/reply
       :timestamp (datetime/timestamp->relative timestamp)
       :unread?   (not read)
       :context   [[common/user-avatar-tag author]
                   [quo/text {:style style/lowercase-text} (i18n/label :t/on)]
                   (if is-chat-from-community?
                     [quo/context-tag tag-params {:uri community-image} community-name chat-name]
                     [quo/group-avatar-tag chat-name tag-params])]
       :message   {:body-number-of-lines 1
                   :body                 (get-message-content message)}}]]))
