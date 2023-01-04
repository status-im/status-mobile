(ns status-im2.contexts.activity-center.notification.mentions.view
  (:require [clojure.string :as string]
            [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.datetime :as datetime]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [status-im2.contexts.activity-center.notification.mentions.style :as style]
            [utils.re-frame :as rf]))

(defn message-body
  [message]
  (let [parsed-text          (get-in message [:content :parsed-text])
        parsed-text-children (:children (first parsed-text))]
    (into [quo/text
           {:number-of-lines     2
            :style               style/tag-text
            :accessibility-label :activity-message-body
            :size                :paragraph-1}]
          (map-indexed (fn [index {:keys [type literal]}]
                         ^{:key index}
                         (case type
                           "mention" [quo/text
                                      {:style style/mention-text
                                       :size  :paragraph-1}
                                      (str "@" (rf/sub [:contacts/contact-name-by-identity literal]))]
                           literal))
                       parsed-text-children))))

(defn view
  [{:keys [author chat-name chat-id message] :as notification}]
  [rn/touchable-without-feedback
   {:on-press (fn []
                (rf/dispatch [:hide-popover])
                (rf/dispatch [:chat.ui/navigate-to-chat chat-id]))}
   [quo/activity-log
    {:title     (i18n/label :t/mention)
     :icon      :i/mention
     :timestamp (datetime/timestamp->relative (:timestamp notification))
     :unread?   (not (:read notification))
     :context   [[common/user-avatar-tag author]
                 [quo/text {:style style/tag-text} (string/lower-case (i18n/label :t/on))]
                 ;; TODO (@smohamedjavid): The `group-avatar-tag` component
                 ;; does NOT support displaying channel name along with community/chat name.
                 ;; Need to update the component to support it.
                 [quo/group-avatar-tag chat-name
                  {:size           :small
                   :override-theme :dark
                   :color          colors/primary-50
                   :style          style/tag
                   :text-style     style/tag-text}]]
     :message   {:body (message-body message)}}]])
