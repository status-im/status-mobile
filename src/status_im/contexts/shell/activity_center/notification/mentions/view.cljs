(ns status-im.contexts.shell.activity-center.notification.mentions.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.gesture :as gesture]
    [status-im.contexts.shell.activity-center.notification.common.view :as common]
    [status-im.contexts.shell.activity-center.notification.mentions.style :as style]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- mention-text
  [literal]
  (let [mention (rf/sub [:messages/resolve-mention literal])]
    [quo/text
     {:style style/mention-text
      :size  :paragraph-1}
     (str "@" mention)]))

(defn- parsed-text->hiccup
  [{:keys [literal destination] message-type :type}]
  (case message-type
    "mention" [mention-text literal]
    "link"    destination
    literal))

(defn- message-body
  [{:keys [container parsed-text]}]
  (into container (map parsed-text->hiccup) parsed-text))

(defn- simple-message
  [content]
  (let [parsed-text (get-in content [:content :parsed-text 0 :children])]
    [message-body
     {:container   [quo/text
                    {:number-of-lines     1
                     :style               style/tag-text
                     :accessibility-label :activity-message-body
                     :size                :paragraph-1}]
      :parsed-text parsed-text}]))

(defn- album-message
  [content]
  (let [parsed-text (get-in content [0 :parsedText 0 :children])
        images      (map :image content)]
    [quo/activity-logs-photos
     {:photos       images
      :message-text [message-body
                     {:container   [:<>]
                      :parsed-text parsed-text}]}]))

(defn- swipeable
  [{:keys [extra-fn]} child]
  [common/swipeable
   {:left-button    common/swipe-button-read-or-unread
    :left-on-press  common/swipe-on-press-toggle-read
    :right-button   common/swipe-button-delete
    :right-on-press common/swipe-on-press-delete
    :extra-fn       extra-fn}
   child])

(defn view
  [{:keys [notification extra-fn]}]
  (let [{:keys [author chat-name community-id chat-id message read timestamp
                album-messages]} notification
        community-chat?          (not (string/blank? community-id))
        community-name           (rf/sub [:communities/name community-id])
        community-logo           (rf/sub [:communities/logo community-id])
        customization-color      (rf/sub [:profile/customization-color])]
    [swipeable {:extra-fn extra-fn}
     [gesture/touchable-without-feedback
      {:on-press (fn []
                   (rf/dispatch [:hide-popover])
                   (rf/dispatch [:chat/pop-to-root-and-navigate-to-chat chat-id]))}
      [quo/activity-log
       {:title               (i18n/label :t/mention)
        :customization-color customization-color
        :icon                :i/mention
        :timestamp           (datetime/timestamp->relative timestamp)
        :unread?             (not read)
        :context             [[common/user-avatar-tag author]
                              [quo/text {:style style/tag-text}
                               (string/lower-case (i18n/label :t/on-capitalized))]
                              (if community-chat?
                                [quo/context-tag
                                 {:type           :channel
                                  :blur?          true
                                  :size           24
                                  :community-logo community-logo
                                  :community-name community-name
                                  :channel-name   chat-name}]
                                [quo/context-tag
                                 {:type       :group
                                  :group-name chat-name
                                  :blur?      true
                                  :size       24}])]
        :message             {:body (if album-messages
                                      [album-message album-messages]
                                      [simple-message message])}}]]]))
