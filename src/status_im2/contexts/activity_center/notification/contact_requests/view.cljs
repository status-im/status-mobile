(ns status-im2.contexts.activity-center.notification.contact-requests.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.constants :as constants]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [status-im2.contexts.activity-center.notification.contact-requests.events]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn outgoing-contact-request-view
  [{:keys [id chat-id message last-message] :as notification}]
  (let [message (or message last-message)]
    [rn/view
     [quo/activity-log
      {:title     (i18n/label :t/contact-request)
       :icon      :main-icons2/add-user
       :timestamp (datetime/timestamp->relative (:timestamp notification))
       :unread?   (not (:read notification))
       :context   [(i18n/label :t/contact-request-outgoing)
                   [common/user-avatar-tag chat-id]]
       :message   {:body (get-in message [:content :text])}
       :button-1  {:label               (i18n/label :t/cancel)
                   :accessibility-label :cancel-contact-request
                   :type                :danger
                   :on-press            (fn []
                                          (rf/dispatch [:activity-center.contact-requests/cancel-outgoing-request id])
                                          (rf/dispatch [:activity-center.notifications/mark-as-read
                                                        id]))}}]]))

(defn incoming-contact-request-view
  [{:keys [id author message last-message] :as notification}]
  (let [message (or message last-message)]
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
                      constants/contact-request-message-state-none
                      {:type :negative :label "Pending"}
                      nil)}
        (case (:contact-request-state message)
          constants/contact-request-message-state-pending
          {:button-1 {:label               (i18n/label :t/decline)
                      :accessibility-label :decline-contact-request
                      :type                :danger
                      :on-press            (fn []
                                             (rf/dispatch [:activity-center.contact-requests/decline-request id])
                                             (rf/dispatch [:activity-center.notifications/mark-as-read
                                                           id]))}
           :button-2 {:label               (i18n/label :t/accept)
                      :accessibility-label :accept-contact-request
                      :type                :positive
                      :on-press            (fn []
                                             (rf/dispatch [:activity-center.contact-requests/accept-request id])
                                             (rf/dispatch [:activity-center.notifications/mark-as-read
                                                           id]))}}
          nil))]]))

(defn view [{:keys [author message last-message] :as notification}]
  (let [{:keys [public-key]} (rf/sub [:multiaccount/contact])
        {:keys [contact-request-state]} (or message last-message)]
    (cond
      (= public-key author)
      [outgoing-contact-request-view notification]

      (= contact-request-state constants/contact-request-message-state-accepted)
      [rn/touchable-opacity
       {:on-press (fn []
                    (rf/dispatch [:hide-popover])
                    (rf/dispatch [:chat.ui/start-chat {:public-key author}]))}
       [incoming-contact-request-view notification]]

      :default
      [incoming-contact-request-view notification])))
