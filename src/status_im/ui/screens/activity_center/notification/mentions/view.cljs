(ns status-im.ui.screens.activity-center.notification.mentions.view
  (:require [quo.components.animated.pressable :as animation]
            [quo2.core :as quo2]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.activity-center.notification.mentions.style :as style]
            [status-im.ui.screens.activity-center.utils :as activity-center.utils]
            [status-im.utils.datetime :as datetime]
            [utils.re-frame :as rf]
            [clojure.string :as str]))

(defn message-body [message]
  (let [parsed-text (get-in message [:content :parsed-text])
        parsed-text-children (:children (first parsed-text))]
    [text/text {:number-of-lines     2
                :style               style/tag-text
                :accessibility-label :activity-message-body
                :size                :paragraph-1}
     (for [{:keys [type literal]} parsed-text-children]
       (case type
         "mention" [text/text {:style style/mention-text
                               :size  :paragraph-1}
                    (str "@" (rf/sub [:contacts/contact-name-by-identity literal]))]
         literal))]))

(defn view
  [{:keys [author chat-name chat-id message] :as notification}]
  (let [contact (rf/sub [:contacts/contact-by-identity author])]
    [animation/pressable
     {:on-press (fn []
                  (rf/dispatch [:hide-popover])
                  (rf/dispatch [:chat.ui/navigate-to-chat chat-id]))}
     [quo2/activity-log
      {:title     (i18n/label :t/mention)
       :icon      :main-icons2/mention
       :timestamp (datetime/timestamp->relative (:timestamp notification))
       :unread?   (not (:read notification))
       :context   [[quo2/user-avatar-tag
                    {:color          :purple
                     :override-theme :dark
                     :size           :small
                     :style          style/tag
                     :text-style     style/tag-text}
                    (activity-center.utils/contact-name contact)
                    (multiaccounts/displayed-photo contact)]
                   [quo2/text {:style style/tag-text} (str/lower-case (i18n/label :t/on))]
                  ;;  TODO: The `group-avatar-tag` component does NOT support displaying channel name along with community/chat name. Need to update the component to support it. 
                   [quo2/group-avatar-tag chat-name {:size           :small
                                                     :override-theme :dark
                                                     :color          colors/primary-50
                                                     :style          style/tag
                                                     :text-style     style/tag-text}]]
       :message    {:body (message-body message)}}]]))