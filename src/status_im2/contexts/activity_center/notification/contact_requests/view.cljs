(ns status-im2.contexts.activity-center.notification.contact-requests.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.constants :as constants]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn outgoing-contact-request-view
  [{:keys [id chat-id message last-message] :as notification}]
  (let [{:keys [contact-request-state] :as message} (or message last-message)]
    (if (= contact-request-state constants/contact-request-message-state-accepted)
      [quo/activity-log
       {:title     (i18n/label :t/contact-request-was-accepted)
        :icon      :i/add-user
        :timestamp (datetime/timestamp->relative (:timestamp notification))
        :unread?   (not (:read notification))
        :context   [[common/user-avatar-tag chat-id]
                    (i18n/label :t/contact-request-is-now-a-contact)]}
       :message {:body (get-in message [:content :text])}
       :items []]
      [quo/activity-log
       {:title     (i18n/label :t/contact-request)
        :icon      :i/add-user
        :timestamp (datetime/timestamp->relative (:timestamp notification))
        :unread?   (not (:read notification))
        :context   [(i18n/label :t/contact-request-outgoing)
                    [common/user-avatar-tag chat-id]]
        :message   {:body (get-in message [:content :text])}
        :items     (case contact-request-state
                     constants/contact-request-state-mutual
                     [{:type                :button
                       :subtype             :danger
                       :key                 :button-cancel
                       :label               (i18n/label :t/cancel)
                       :accessibility-label :cancel-contact-request
                       :on-press            (fn []
                                              (rf/dispatch
                                               [:activity-center.contact-requests/cancel-outgoing-request
                                                (:from message)])
                                              (rf/dispatch [:activity-center.notifications/mark-as-read
                                                            id]))}
                      {:type    :status
                       :subtype :pending
                       :key     :status-pending
                       :label   (i18n/label :t/pending)}]

                     constants/contact-request-message-state-declined
                     [{:type    :status
                       :subtype :pending
                       :key     :status-pending
                       :label   (i18n/label :t/pending)}]

                     nil)}])))

(defn incoming-contact-request-view
  [{:keys [id author message last-message] :as notification}]
  (let [message (or message last-message)]
    [quo/activity-log
     {:title (i18n/label :t/contact-request)
      :icon :i/add-user
      :timestamp (datetime/timestamp->relative (:timestamp notification))
      :unread? (not (:read notification))
      :context [[common/user-avatar-tag author]
                (i18n/label :t/contact-request-sent)]
      :message {:body (get-in message [:content :text])}
      :items
      (case (:contact-request-state message)
        constants/contact-request-message-state-accepted
        [{:type    :status
          :subtype :positive
          :key     :status-accepted
          :label   (i18n/label :t/accepted)}]

        constants/contact-request-message-state-declined
        [{:type    :status
          :subtype :negative
          :key     :status-declined
          :label   (i18n/label :t/declined)}]

        constants/contact-request-state-mutual
        [{:type                :button
          :subtype             :danger
          :key                 :button-decline
          :label               (i18n/label :t/decline)
          :accessibility-label :decline-contact-request
          :on-press            (fn []
                                 (rf/dispatch [:activity-center.contact-requests/decline-request id])
                                 (rf/dispatch [:activity-center.notifications/mark-as-read
                                               id]))}
         {:type                :button
          :subtype             :positive
          :key                 :button-accept
          :label               (i18n/label :t/accept)
          :accessibility-label :accept-contact-request
          :on-press            (fn []
                                 (rf/dispatch [:activity-center.contact-requests/accept-request id])
                                 (rf/dispatch [:activity-center.notifications/mark-as-read
                                               id]))}]
        nil)}]))

(defn view
  [{:keys [author message last-message] :as notification}]
  (let [{:keys [public-key]}            (rf/sub [:multiaccount/contact])
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

      :else
      [incoming-contact-request-view notification])))
