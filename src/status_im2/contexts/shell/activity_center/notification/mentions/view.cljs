(ns status-im2.contexts.shell.activity-center.notification.mentions.view
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [react-native.gesture :as gesture]
            [status-im2.contexts.shell.activity-center.notification.common.view :as common]
            [status-im2.contexts.shell.activity-center.notification.mentions.style :as style]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- message-body
  [message]
  (let [parsed-text          (get-in message [:content :parsed-text])
        parsed-text-children (:children (first parsed-text))]
    (into [quo/text
           {:number-of-lines     1
            :style               style/tag-text
            :accessibility-label :activity-message-body
            :size                :paragraph-1}]
          (map-indexed (fn [index {:keys [type literal]}]
                         ^{:key index}
                         (case type
                           "mention" [quo/text
                                      {:style style/mention-text
                                       :size  :paragraph-1}
                                      (str "@" (rf/sub [:messages/resolve-mention literal]))]
                           literal))
                       parsed-text-children))))

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
                message read timestamp]} notification
        community-chat?                  (not (string/blank? community-id))
        community                        (rf/sub [:communities/community community-id])
        community-name                   (:name community)
        community-image                  (get-in community [:images :thumbnail :uri])]
    [swipeable props
     [gesture/touchable-without-feedback
      {:on-press (fn []
                   (rf/dispatch [:hide-popover])
                   (rf/dispatch [:chat/navigate-to-chat chat-id]))}
      [quo/activity-log
       {:title               (i18n/label :t/mention)
        :customization-color customization-color
        :on-layout           set-swipeable-height
        :icon                :i/mention
        :timestamp           (datetime/timestamp->relative timestamp)
        :unread?             (not read)
        :context             [[common/user-avatar-tag author]
                              [quo/text {:style style/tag-text} (string/lower-case (i18n/label :t/on))]
                              (if community-chat?
                                [quo/context-tag common/tag-params community-image community-name
                                 chat-name]
                                [quo/group-avatar-tag chat-name common/tag-params])]
        :message             {:body (message-body message)}}]]]))
