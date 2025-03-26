(ns status-im.contexts.shell.activity-center.notification.contact-requests.view
  (:require
    quo.context
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.constants :as constants]
    [status-im.contexts.shell.activity-center.notification.common.style :as common-style]
    [status-im.contexts.shell.activity-center.notification.common.view :as common]
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
  [{:keys [extra-fn notification]} child]
  (let [{:keys [id author message]}     notification
        {:keys [contact-request-state]} message
        {:keys [public-key]}            (rf/sub [:multiaccount/contact])
        outgoing?                       (= public-key author)
        accept                          (rn/use-callback
                                         #(rf/dispatch [:activity-center.contact-requests/accept id])
                                         [id])
        decline                         (rn/use-callback
                                         #(rf/dispatch [:activity-center.contact-requests/decline id])
                                         [id])]
    (cond
      (or (#{constants/contact-request-message-state-accepted
             constants/contact-request-message-state-declined}
           contact-request-state)
          (and outgoing? (= contact-request-state constants/contact-request-message-state-pending)))
      [common/swipeable
       {:left-button    common/swipe-button-read-or-unread
        :left-on-press  common/swipe-on-press-toggle-read
        :right-button   common/swipe-button-delete
        :right-on-press common/swipe-on-press-delete
        :extra-fn       extra-fn}
       child]

      (and (= contact-request-state constants/contact-request-message-state-pending)
           (not outgoing?))
      [common/swipeable
       {:left-button    swipe-button-accept
        :left-on-press  accept
        :right-button   swipe-button-decline
        :right-on-press decline
        :extra-fn       extra-fn}
       child]

      :else
      child)))

(defn- outgoing-contact-request-view
  [{:keys [notification]} theme]
  (let [{:keys [chat-id message last-message accepted]} notification
        {:keys [contact-request-state] :as message}     (or message last-message)
        customization-color                             (rf/sub [:profile/customization-color])]
    (if accepted
      [quo/activity-log
       {:title               (i18n/label :t/contact-request-was-accepted)
        :customization-color customization-color
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
        :icon                :i/add-user
        :timestamp           (datetime/timestamp->relative (:timestamp notification))
        :unread?             (not (:read notification))
        :context             [(i18n/label :t/contact-request-outgoing)
                              [common/user-avatar-tag chat-id]]
        :message             {:body (get-in message [:content :text])}
        :items               (condp = contact-request-state
                               constants/contact-request-message-state-pending
                               [{:type    :status
                                 :subtype :pending
                                 :key     :status-pending
                                 :blur?   true
                                 :label   (i18n/label :t/pending)
                                 :theme   theme}]

                               constants/contact-request-message-state-declined
                               [{:type    :status
                                 :subtype :pending
                                 :key     :status-pending
                                 :blur?   true
                                 :label   (i18n/label :t/pending)
                                 :theme   theme}]
                               nil)}])))

(defn- incoming-contact-request-view
  [{:keys [notification]} theme]
  (let [{:keys [id author message
                last-message]} notification
        customization-color    (rf/sub [:profile/customization-color])
        message                (or message last-message)
        accept                 (rn/use-callback
                                (fn []
                                  (rf/dispatch [:activity-center.contact-requests/accept id]))
                                [id])
        decline                (rn/use-callback
                                (fn []
                                  (rf/dispatch [:activity-center.contact-requests/decline id]))
                                [id])]
    [quo/activity-log
     {:title (i18n/label :t/contact-request)
      :customization-color customization-color
      :icon :i/add-user
      :timestamp (datetime/timestamp->relative (:timestamp notification))
      :unread? (not (:read notification))
      :context [[common/user-avatar-tag author]
                (i18n/label :t/contact-request-sent)]
      :message {:body                 (get-in message [:content :text])
                :body-number-of-lines 2}
      :items
      (condp = (:contact-request-state message)
        constants/contact-request-message-state-accepted
        [{:type    :status
          :subtype :positive
          :key     :status-accepted
          :blur?   true
          :label   (i18n/label :t/accepted)
          :theme   theme}]

        constants/contact-request-message-state-declined
        [{:type    :status
          :subtype :negative
          :key     :status-declined
          :blur?   true
          :label   (i18n/label :t/declined)
          :theme   theme}]

        constants/contact-request-message-state-pending
        [{:type                :button
          :subtype             :danger
          :key                 :button-decline
          :label               (i18n/label :t/decline)
          :accessibility-label :decline-contact-request
          :on-press            decline}
         {:type                :button
          :subtype             :positive
          :key                 :button-accept
          :label               (i18n/label :t/accept)
          :accessibility-label :accept-contact-request
          :on-press            accept}]

        nil)}]))

(defn view
  [{:keys [notification] :as props}]
  (let [{:keys [author message last-message]} notification
        {:keys [public-key]}                  (rf/sub [:multiaccount/contact])
        {:keys [contact-request-state]}       (or message last-message)
        theme                                 (quo.context/use-theme)]
    [swipeable props
     (cond
       (= public-key author)
       [outgoing-contact-request-view props theme]

       (= contact-request-state constants/contact-request-message-state-accepted)
       [rn/pressable
        {:on-press #(rf/dispatch [:chat.ui/start-chat author])}
        [incoming-contact-request-view props theme]]

       :else
       [incoming-contact-request-view props theme])]))
