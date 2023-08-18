(ns status-im2.contexts.shell.activity-center.notification.contact-requests.view
  (:require
    [quo2.core :as quo]
    [react-native.gesture :as gesture]
    [status-im2.constants :as constants]
    [status-im2.contexts.shell.activity-center.notification.common.style :as common-style]
    [status-im2.contexts.shell.activity-center.notification.common.view :as common]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- swipe-button-accept
  [{:keys [style]} _]
  [common/swipe-button-container
   {:style (common-style/swipe-success-container style)
    :icon  :i/done
    :text  (i18n/label :t/accept)}])

(defn- swipe-button-decline
  [{:keys [style]} _]
  [common/swipe-button-container
   {:style (common-style/swipe-danger-container style)
    :icon  :i/decline
    :text  (i18n/label :t/decline)}])

(defn- swipeable
  [{:keys [active-swipeable extra-fn notification]} child]
  (let [{:keys [id author message]}     notification
        {:keys [contact-request-state]} message
        {:keys [public-key]}            (rf/sub [:multiaccount/contact])
        outgoing?                       (= public-key author)]
    (cond
      (or (#{constants/contact-request-message-state-accepted
             constants/contact-request-message-state-declined}
           contact-request-state)
          (and outgoing? (= contact-request-state constants/contact-request-message-state-pending)))
      [common/swipeable
       {:left-button      common/swipe-button-read-or-unread
        :left-on-press    common/swipe-on-press-toggle-read
        :right-button     common/swipe-button-delete
        :right-on-press   common/swipe-on-press-delete
        :active-swipeable active-swipeable
        :extra-fn         extra-fn}
       child]

      (and (= contact-request-state constants/contact-request-message-state-pending)
           (not outgoing?))
      [common/swipeable
       {:left-button      swipe-button-accept
        :left-on-press    #(rf/dispatch [:activity-center.contact-requests/accept id])
        :right-button     swipe-button-decline
        :right-on-press   #(rf/dispatch [:activity-center.contact-requests/decline id])
        :active-swipeable active-swipeable
        :extra-fn         extra-fn}
       child]

      :else
      child)))

(defn- outgoing-contact-request-view
  [{:keys [notification set-swipeable-height customization-color]}]
  (let [{:keys [chat-id message last-message]}      notification
        {:keys [contact-request-state] :as message} (or message last-message)]
    (if (= contact-request-state constants/contact-request-message-state-accepted)
      [quo/activity-log
       {:title               (i18n/label :t/contact-request-was-accepted)
        :customization-color customization-color
        :on-layout           set-swipeable-height
        :icon                :i/add-user
        :timestamp           (datetime/timestamp->relative (:timestamp notification))
        :unread?             (not (:read notification))
        :context             [[common/user-avatar-tag chat-id]
                              (i18n/label :t/contact-request-is-now-a-contact)]}
       :message {:body (get-in message [:content :text])}
       :items []]
      [quo/activity-log
       {:title               (i18n/label :t/contact-request)
        :customization-color customization-color
        :on-layout           set-swipeable-height
        :icon                :i/add-user
        :timestamp           (datetime/timestamp->relative (:timestamp notification))
        :unread?             (not (:read notification))
        :context             [(i18n/label :t/contact-request-outgoing)
                              [common/user-avatar-tag chat-id]]
        :message             {:body (get-in message [:content :text])}
        :items               (case contact-request-state
                               constants/contact-request-message-state-pending
                               [{:type    :status
                                 :subtype :pending
                                 :key     :status-pending
                                 :blur?   true
                                 :label   (i18n/label :t/pending)}]

                               constants/contact-request-message-state-declined
                               [{:type    :status
                                 :subtype :pending
                                 :key     :status-pending
                                 :blur?   true
                                 :label   (i18n/label :t/pending)}]

                               nil)}])))

(defn- incoming-contact-request-view
  [{:keys [notification set-swipeable-height customization-color]}]
  (let [{:keys [id author message last-message]} notification
        message                                  (or message last-message)]
    [quo/activity-log
     {:title (i18n/label :t/contact-request)
      :customization-color customization-color
      :on-layout set-swipeable-height
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
          :blur?   true
          :label   (i18n/label :t/accepted)}]

        constants/contact-request-message-state-declined
        [{:type    :status
          :subtype :negative
          :key     :status-declined
          :blur?   true
          :label   (i18n/label :t/declined)}]

        constants/contact-request-message-state-pending
        [{:type                :button
          :subtype             :danger
          :key                 :button-decline
          :label               (i18n/label :t/decline)
          :accessibility-label :decline-contact-request
          :on-press            #(rf/dispatch [:activity-center.contact-requests/decline id])}
         {:type                :button
          :subtype             :positive
          :key                 :button-accept
          :label               (i18n/label :t/accept)
          :accessibility-label :accept-contact-request
          :on-press            #(rf/dispatch [:activity-center.contact-requests/accept id])}]
        nil)}]))

(defn view
  [{:keys [notification] :as props}]
  (let [{:keys [author message last-message]} notification
        {:keys [public-key]}                  (rf/sub [:multiaccount/contact])
        {:keys [contact-request-state]}       (or message last-message)]
    [swipeable props
     (cond
       (= public-key author)
       [outgoing-contact-request-view props]

       (= contact-request-state constants/contact-request-message-state-accepted)
       [gesture/touchable-without-feedback
        {:on-press (fn []
                     (rf/dispatch [:hide-popover])
                     (rf/dispatch [:chat.ui/start-chat author]))}
        [incoming-contact-request-view props]]

       :else
       [incoming-contact-request-view props])]))
