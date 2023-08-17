(ns status-im2.contexts.shell.activity-center.notification.contact-verification.view
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [status-im2.constants :as constants]
            [status-im2.contexts.shell.activity-center.notification.common.style :as common-style]
            [status-im2.contexts.shell.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- swipe-button-decline
  [{:keys [style]} _]
  [common/swipe-button-container
   {:style (common-style/swipe-danger-container style)
    :icon  :i/decline
    :text  (i18n/label :t/decline)}])

(defn- swipe-button-reply
  [{:keys [style]} _]
  [common/swipe-button-container
   {:style (common-style/swipe-primary-container style)
    :icon  :i/reply
    :text  (i18n/label :t/message-reply)}])

(defn- swipe-button-untrustworthy
  [{:keys [style]} _]
  [common/swipe-button-container
   {:style (common-style/swipe-danger-container style)
    :icon  :i/alert
    :text  (i18n/label :t/untrustworthy)}])

(defn- swipe-button-trust
  [{:keys [style]} _]
  [common/swipe-button-container
   {:style (common-style/swipe-success-container style)
    :icon  :i/done
    :text  (i18n/label :t/accept)}])

(defn- context-tags
  [challenger? {:keys [author contact-verification-status]}]
  [[common/user-avatar-tag author]
   (if challenger?
     (when (or (= contact-verification-status constants/contact-verification-status-accepted)
               (= contact-verification-status constants/contact-verification-status-trusted)
               (= contact-verification-status constants/contact-verification-status-untrustworthy))
       (str (string/lower-case (i18n/label :t/replied)) ":"))
     (when (or (= contact-verification-status constants/contact-verification-status-accepted)
               (= contact-verification-status constants/contact-verification-status-pending)
               (= contact-verification-status constants/contact-verification-status-declined))
       (str (i18n/label :t/identity-verification-request-sent) ":")))])

(defn- activity-message
  [challenger? {:keys [contact-verification-status message reply-message]}]
  (if challenger?
    (when (or (= contact-verification-status constants/contact-verification-status-accepted)
              (= contact-verification-status constants/contact-verification-status-trusted)
              (= contact-verification-status constants/contact-verification-status-untrustworthy))
      {:title (get-in message [:content :text])
       :body  (get-in reply-message [:content :text])})
    (when (or (= contact-verification-status constants/contact-verification-status-accepted)
              (= contact-verification-status constants/contact-verification-status-pending)
              (= contact-verification-status constants/contact-verification-status-declined))
      {:body (get-in message [:content :text])})))

(def ^:private max-reply-length
  280)

(defn- valid-reply?
  [reply]
  (<= (count reply) max-reply-length))

(def ^:private invalid-reply?
  (comp not valid-reply?))

(declare view)

(defn- decline-challenge
  [id]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch [:activity-center.contact-verification/decline id])
  (rf/dispatch [:activity-center.notifications/mark-as-read id]))

(defn- prepare-challenge-reply
  [props]
  (rf/dispatch [:show-bottom-sheet
                {:content view
                 :theme   :dark}
                (assoc props :replying? true)]))

(defn- send-challenge-reply
  [id reply]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch [:activity-center.contact-verification/reply id reply])
  (rf/dispatch [:activity-center.notifications/mark-as-read id]))

(defn- mark-challenge-untrustworthy
  [id]
  (rf/dispatch [:activity-center.contact-verification/mark-as-untrustworthy id])
  (rf/dispatch [:activity-center.notifications/mark-as-read id]))

(defn- mark-challenge-trusted
  [id]
  (rf/dispatch [:activity-center.contact-verification/mark-as-trusted id])
  (rf/dispatch [:activity-center.notifications/mark-as-read id]))

(defn- swipeable
  [{:keys [active-swipeable extra-fn notification replying?] :as props} child]
  (let [{:keys [id message
                contact-verification-status]} notification
        challenger?                           (:outgoing message)]
    (cond
      replying?
      child

      (and (not challenger?)
           (= contact-verification-status constants/contact-verification-status-pending))
      [common/swipeable
       {:left-button      swipe-button-reply
        :left-on-press    #(prepare-challenge-reply props)
        :right-button     swipe-button-decline
        :right-on-press   #(decline-challenge id)
        :active-swipeable active-swipeable
        :extra-fn         extra-fn}
       child]

      (and challenger?
           (= contact-verification-status constants/contact-verification-status-accepted))
      [common/swipeable
       {:left-button      swipe-button-trust
        :left-on-press    #(mark-challenge-trusted id)
        :right-button     swipe-button-untrustworthy
        :right-on-press   #(mark-challenge-untrustworthy id)
        :active-swipeable active-swipeable
        :extra-fn         extra-fn}
       child]

      (#{constants/contact-verification-status-accepted
         constants/contact-verification-status-declined
         constants/contact-verification-status-trusted}
       contact-verification-status)
      [common/swipeable
       {:left-button      common/swipe-button-read-or-unread
        :left-on-press    common/swipe-on-press-toggle-read
        :right-button     common/swipe-button-delete
        :right-on-press   common/swipe-on-press-delete
        :active-swipeable active-swipeable
        :extra-fn         extra-fn}
       child]

      :else
      child)))

(defn view
  [_]
  (let [reply (atom "")]
    (fn [{:keys [notification set-swipeable-height replying? customization-color] :as props}]
      (let [{:keys [id message
                    contact-verification-status]} notification
            challenger?                           (:outgoing message)]
        ;; TODO(@ilmotta): Declined challenges should only be displayed for the
        ;; challengee, not the challenger.
        ;; https://github.com/status-im/status-mobile/issues/14354
        (when-not
          (and challenger?
               (= contact-verification-status constants/contact-verification-status-declined))
          [swipeable props
           [quo/activity-log
            (merge
             (when-not replying?
               {:on-layout set-swipeable-height})
             {:title (i18n/label :t/identity-verification-request)
              :customization-color customization-color
              :icon :i/friend
              :timestamp (datetime/timestamp->relative (:timestamp notification))
              :unread? (not (:read notification))
              :on-update-reply #(reset! reply %)
              :replying? replying?
              :max-reply-length max-reply-length
              :valid-reply? valid-reply?
              :context (context-tags challenger? notification)
              :message (activity-message challenger? notification)
              :items
              (cond-> []
                (and challenger?
                     (= contact-verification-status constants/contact-verification-status-accepted))
                (concat
                 [{:type                :button
                   :subtype             :danger
                   :key                 :button-mark-as-untrustworthy
                   :label               (i18n/label :t/untrustworthy)
                   :accessibility-label :mark-contact-verification-as-untrustworthy
                   :on-press            #(mark-challenge-untrustworthy id)}
                  {:type                :button
                   :subtype             :positive
                   :key                 :button-accept
                   :label               (i18n/label :t/accept)
                   :accessibility-label :mark-contact-verification-as-trusted
                   :on-press            #(mark-challenge-trusted id)}])

                (and challenger?
                     (= contact-verification-status constants/contact-verification-status-trusted))
                (concat [{:type    :status
                          :subtype :positive
                          :key     :status-trusted
                          :label   (i18n/label :t/status-confirmed)}])

                (and challenger?
                     (= contact-verification-status constants/contact-verification-status-untrustworthy))
                (concat [{:type    :status
                          :subtype :negative
                          :key     :status-untrustworthy
                          :label   (i18n/label :t/untrustworthy)}])

                (and (not challenger?)
                     (= contact-verification-status constants/contact-verification-status-accepted))
                (concat [{:type    :status
                          :subtype :positive
                          :key     :status-accepted
                          :label   (i18n/label :t/replied)}])

                (and (not challenger?)
                     (= contact-verification-status constants/contact-verification-status-declined))
                (concat [{:type    :status
                          :subtype :negative
                          :key     :status-declined
                          :label   (i18n/label :t/declined)}])

                (and (not challenger?)
                     (= contact-verification-status constants/contact-verification-status-pending))
                (concat
                 [{:type                :button
                   :subtype             :danger
                   :key                 :button-decline
                   :label               (i18n/label :t/decline)
                   :accessibility-label :decline-contact-verification
                   :on-press            #(decline-challenge id)}
                  (if replying?
                    {:type                :button
                     :subtype             :primary
                     :key                 :button-reply
                     :label               (i18n/label :t/send-reply)
                     :accessibility-label :reply-to-contact-verification
                     :disable-when        invalid-reply?
                     :on-press            #(send-challenge-reply id @reply)}
                    {:type                :button
                     :subtype             :primary
                     :key                 :button-send-reply
                     :label               (i18n/label :t/message-reply)
                     :accessibility-label :send-reply-to-contact-verification
                     :on-press            #(prepare-challenge-reply props)})]))})]])))))
