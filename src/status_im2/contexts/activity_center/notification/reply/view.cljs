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

;; NOTE: Replies support text, image and stickers only.
(defn get-message-content
  [{:keys [content-type] :as message}]
  (case content-type
    constants/content-type-text    (get-in message [:content :text])

    constants/content-type-image   [old-message/message-content-image message]

    constants/content-type-sticker [old-message/sticker message]))

(defn view
  [{:keys [author chat-name chat-id message read timestamp]}]
  [rn/touchable-opacity
   {:on-press (fn []
                (rf/dispatch [:hide-popover])
                (rf/dispatch [:chat.ui/navigate-to-chat chat-id]))}
   [quo/activity-log
    {:title     (i18n/label :t/message-reply)
     :icon      :i/reply
     :timestamp (datetime/timestamp->relative timestamp)
     :unread?   (not read)
     :context   [[common/user-avatar-tag author]
                 [quo/text {:style style/lowercase-text} (i18n/label :t/on)]
                 [quo/group-avatar-tag chat-name
                  {:size           :small
                   :override-theme :dark
                   :color          colors/primary-50
                   :style          style/tag
                   :text-style     style/tag-text}]]
     :message   {:body-number-of-lines 1
                 :body                 (get-message-content message)}}]])
