(ns status-im2.contexts.shell.activity-center.notification.reply.view
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [react-native.gesture :as gesture]
            [status-im.ui.screens.chat.message.legacy-view :as old-message]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.constants :as constants]
            [status-im2.contexts.shell.activity-center.notification.common.view :as common]
            [status-im2.contexts.shell.activity-center.notification.reply.style :as style]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.url :as url]))

;; NOTE: Replies support text, image and stickers only.
(defn- get-message-content
  [{:keys [content-type] :as message} albumMessages]
  (case content-type
    constants/content-type-text [quo/text {:style style/tag-text}
                                 (get-in message [:content :text])]

    constants/content-type-image
    (let [images           (or albumMessages message)
          image-urls       (if albumMessages
                             (mapv #(get % :image) images)
                             [(get-in message [:content :image])])
          image-local-urls (mapv (fn [url]
                                   {:uri (url/replace-port url (rf/sub [:mediaserver/port]))})
                                 image-urls)
          photos           (when image-local-urls image-local-urls)]
      [quo/activity-logs-photos
       {:photos photos
        :text   (get-in message [:content :text])}])

    constants/content-type-sticker [old-message/sticker message]

    constants/content-type-system-pinned-message
    [not-implemented/not-implemented
     [quo/text {:style style/tag-text}
      (get-in message [:content :text])]]

    ;; NOTE: The following type (system-text) doesn't have a design yet.
    ;; https://github.com/status-im/status-mobile/issues/14915
    constants/content-type-system-text [not-implemented/not-implemented
                                        [quo/text {:style style/tag-text}
                                         (get-in message [:content :text])]]

    nil))

(defn- swipeable
  [{:keys [active-swipeable extra-fn]} child]
  [common/swipeable
   {:left-button      common/swipe-button-read-or-unread
    :left-on-press    common/swipe-on-press-toggle-read
    :right-button     common/swipe-button-delete
    :right-on-press   common/swipe-on-press-delete
    :active-swipeable active-swipeable
    :extra-fn         extra-fn}
   child])

(defn view
  [{:keys [notification set-swipeable-height customization-color] :as props}]
  (let [{:keys [author chat-name community-id chat-id
                message read timestamp albumMessages]} notification
        community-chat?                                (not (string/blank? community-id))
        community                                      (rf/sub [:communities/community community-id])
        community-name                                 (:name community)
        community-image                                (get-in community [:images :thumbnail :uri])]
    [swipeable props
     [gesture/touchable-without-feedback
      {:on-press (fn []
                   (rf/dispatch [:hide-popover])
                   (rf/dispatch [:chat/navigate-to-chat chat-id]))}
      [quo/activity-log
       {:title               (i18n/label :t/message-reply)
        :customization-color customization-color
        :on-layout           set-swipeable-height
        :icon                :i/reply
        :timestamp           (datetime/timestamp->relative timestamp)
        :unread?             (not read)
        :context             [[common/user-avatar-tag author]
                              [quo/text {:style style/lowercase-text} (i18n/label :t/on)]
                              (if community-chat?
                                [quo/context-tag
                                 {:type           :channel
                                  :blur?          true
                                  :size           24
                                  :community-logo community-image
                                  :community-name community-name
                                  :channel-name   chat-name}]
                                [quo/context-tag
                                 {:type       :group
                                  :group-name chat-name
                                  :blur?      true
                                  :size       24}])]
        :message             {:body-number-of-lines 1
                              :attachment           (cond
                                                      (= (:content-type message)
                                                         constants/content-type-text)
                                                      :text

                                                      (= (:content-type message)
                                                         constants/content-type-image)
                                                      :photo

                                                      (= (:content-type message)
                                                         constants/content-type-sticker)
                                                      :sticker

                                                      (= (:content-type message)
                                                         constants/content-type-gif)
                                                      :gif

                                                      :else
                                                      nil)
                              :body                 (get-message-content message albumMessages)}}]]]))
