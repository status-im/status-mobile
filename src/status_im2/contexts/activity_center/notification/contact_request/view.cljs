(ns status-im2.contexts.activity-center.notification.contact-request.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.constants :as constants]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [id author message last-message] :as notification}]
  (let [message   (or message last-message)
        pressable (case (:contact-request-state message)
                    constants/contact-request-message-state-accepted
                    ;; NOTE(2022-09-21): We need to dispatch to
                    ;; `:chat.ui/start-chat` instead of
                    ;; `:chat/navigate-to-chat`, otherwise the chat screen
                    ;; looks completely broken if it has never been opened
                    ;; before for the accepted contact.
                    [rn/touchable-opacity
                     {:on-press (fn []
                                  (rf/dispatch [:hide-popover])
                                  (rf/dispatch [:chat.ui/start-chat
                                                {:public-key author}]))}]
                    [:<>])]
    (conj
     pressable
     [rn/view
      [quo/activity-log
       (merge
        {:title     (i18n/label :t/contact-request)
         :icon      :main-icons2/add-user
         :timestamp (datetime/timestamp->relative (:timestamp notification))
         :unread?   (not (:read notification))
         :context   [[common/user-avatar-tag author]
                     (i18n/label :t/contact-request-sent)]
         :message   {:body (get-in message [:content :text])}
         :status    (case (:contact-request-state message)
                      constants/contact-request-message-state-accepted
                      {:type :positive :label (i18n/label :t/accepted)}
                      constants/contact-request-message-state-declined
                      {:type :negative :label (i18n/label :t/declined)}
                      nil)}
        (case (:contact-request-state message)
          constants/contact-request-state-mutual
          {:button-1 {:label               (i18n/label :t/decline)
                      :accessibility-label :decline-contact-request
                      :type                :danger
                      :on-press            (fn []
                                             (rf/dispatch [:contact-requests.ui/decline-request id])
                                             (rf/dispatch [:activity-center.notifications/mark-as-read
                                                           id]))}
           :button-2 {:label               (i18n/label :t/accept)
                      :accessibility-label :accept-contact-request
                      :type                :positive
                      :on-press            (fn []
                                             (rf/dispatch [:contact-requests.ui/accept-request id])
                                             (rf/dispatch [:activity-center.notifications/mark-as-read
                                                           id]))}}
          nil))]])))
