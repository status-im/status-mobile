(ns status-im.ui.screens.activity-center.notification.contact-request.view
  (:require [quo.components.animated.pressable :as animation]
            [quo2.core :as quo2]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.activity-center.notification.contact-request.style :as style]
            [status-im.ui.screens.activity-center.utils :as activity-center.utils]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.re-frame :as rf]))

(defn view
  [{:keys [id] :as notification}]
  (let [message   (or (:message notification) (:last-message notification))
        contact   (rf/sub [:contacts/contact-by-identity (:author notification)])
        pressable (case (:contact-request-state message)
                    constants/contact-request-message-state-accepted
                    ;; NOTE(2022-09-21): We need to dispatch to
                    ;; `:contact.ui/send-message-pressed` instead of
                    ;; `:chat.ui/navigate-to-chat`, otherwise the chat screen
                    ;; looks completely broken if it has never been opened
                    ;; before for the accepted contact.
                    [animation/pressable {:on-press (fn []
                                                      (rf/dispatch [:hide-popover])
                                                      (rf/dispatch [:contact.ui/send-message-pressed {:public-key (:author notification)}]))}]
                    [:<>])]
    (conj pressable
          [quo2/activity-log
           (merge {:title     (i18n/label :t/contact-request)
                   :icon      :main-icons2/add-user
                   :timestamp (datetime/timestamp->relative (:timestamp notification))
                   :unread?   (not (:read notification))
                   :context   [[quo2/user-avatar-tag
                                {:color          :purple
                                 :override-theme :dark
                                 :size           :small
                                 :style          style/user-avatar-tag
                                 :text-style     style/user-avatar-tag-text}
                                (activity-center.utils/contact-name contact)
                                (multiaccounts/displayed-photo contact)]
                               [quo2/text {:style style/context-tag-text}
                                (i18n/label :t/contact-request-sent)]]
                   :message   {:body (get-in message [:content :text])}
                   :status    (case (:contact-request-state message)
                                constants/contact-request-message-state-accepted
                                {:type :positive :label (i18n/label :t/accepted)}
                                constants/contact-request-message-state-declined
                                {:type :negative :label (i18n/label :t/declined)}
                                nil)}
                  (case (:contact-request-state message)
                    constants/contact-request-state-mutual
                    {:button-1 {:label    (i18n/label :t/decline)
                                :type     :danger
                                :on-press #(rf/dispatch [:contact-requests.ui/decline-request id])}
                     :button-2 {:label    (i18n/label :t/accept)
                                :type     :positive
                                :on-press #(rf/dispatch [:contact-requests.ui/accept-request id])}}
                    nil))])))
