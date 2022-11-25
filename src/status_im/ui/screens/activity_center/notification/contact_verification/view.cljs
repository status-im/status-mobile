(ns status-im.ui.screens.activity-center.notification.contact-verification.view
  (:require [clojure.string :as str]
            [quo2.core :as quo2]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.activity-center.notification.contact-verification.style :as style]
            [status-im.ui.screens.activity-center.utils :as activity-center.utils]
            [status-im.utils.datetime :as datetime]
            [utils.re-frame :as rf]))

(defn- hide-bottom-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch [:dismiss-keyboard])
  (rf/dispatch event))

(defn- context-tags
  [challenger? {:keys [author contact-verification-status]}]
  (let [contact (rf/sub [:contacts/contact-by-identity author])]
    [[quo2/user-avatar-tag
      {:color          :purple
       :override-theme :dark
       :size           :small
       :style          style/user-avatar-tag
       :text-style     style/user-avatar-tag-text}
      (activity-center.utils/contact-name contact)
      (multiaccounts/displayed-photo contact)]
     (if challenger?
       (when (or (= contact-verification-status constants/contact-verification-status-accepted)
                 (= contact-verification-status constants/contact-verification-status-trusted)
                 (= contact-verification-status constants/contact-verification-status-untrustworthy))
         (str (str/lower-case (i18n/label :t/replied)) ":"))
       (when (or (= contact-verification-status constants/contact-verification-status-accepted)
                 (= contact-verification-status constants/contact-verification-status-pending)
                 (= contact-verification-status constants/contact-verification-status-declined))
         (str (i18n/label :t/identity-verification-request-sent) ":")))]))

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

(defn- activity-status
  [challenger? contact-verification-status]
  (if challenger?
    (cond (= contact-verification-status constants/contact-verification-status-trusted)
          {:type :positive :label (i18n/label :t/status-confirmed)}
          (= contact-verification-status constants/contact-verification-status-untrustworthy)
          {:type :negative :label (i18n/label :t/untrustworthy)})
    (cond (= contact-verification-status constants/contact-verification-status-accepted)
          {:type :positive :label (i18n/label :t/replied)}
          (= contact-verification-status constants/contact-verification-status-declined)
          {:type :negative :label (i18n/label :t/declined)})))

(defn view
  [_ _]
  (let [reply (atom "")]
    (fn [{:keys [id message contact-verification-status] :as notification} {:keys [replying?]}]
      (let [challenger? (:outgoing message)]
        ;; TODO(@ilmotta): Declined challenges should only be displayed for the
        ;; challengee, not the challenger.
        ;; https://github.com/status-im/status-mobile/issues/14354
        (when-not (and challenger? (= contact-verification-status constants/contact-verification-status-declined))
          [quo2/activity-log
           (merge {:title           (i18n/label :t/identity-verification-request)
                   :icon            :i/friend
                   :timestamp       (datetime/timestamp->relative (:timestamp notification))
                   :unread?         (not (:read notification))
                   :on-update-reply #(reset! reply %)
                   :replying?       replying?
                   :context         (context-tags challenger? notification)
                   :message         (activity-message challenger? notification)
                   :status          (activity-status challenger? contact-verification-status)}
                  (if challenger?
                    (when (= contact-verification-status constants/contact-verification-status-accepted)
                      {:button-1 {:label               (i18n/label :t/untrustworthy)
                                  :accessibility-label :mark-contact-verification-as-untrustworthy
                                  :type                :danger
                                  :on-press            (fn []
                                                         (rf/dispatch [:activity-center.contact-verification/mark-as-untrustworthy id])
                                                         (rf/dispatch [:activity-center.notifications/mark-as-read id]))}
                       :button-2 {:label               (i18n/label :t/accept)
                                  :accessibility-label :mark-contact-verification-as-trusted
                                  :type                :positive
                                  :on-press            (fn []
                                                         (rf/dispatch [:activity-center.contact-verification/mark-as-trusted id])
                                                         (rf/dispatch [:activity-center.notifications/mark-as-read id]))}})
                    (when (= contact-verification-status constants/contact-verification-status-pending)
                      {:button-1 {:label               (i18n/label :t/decline)
                                  :accessibility-label :decline-contact-verification
                                  :type                :danger
                                  :on-press            (fn []
                                                         (hide-bottom-sheet-and-dispatch [:activity-center.contact-verification/decline id])
                                                         (rf/dispatch [:activity-center.notifications/mark-as-read id]))}
                       :button-2 (if replying?
                                   {:label               (i18n/label :t/send-reply)
                                    :accessibility-label :reply-to-contact-verification
                                    :type                :primary
                                    :on-press            (fn []
                                                           (hide-bottom-sheet-and-dispatch [:activity-center.contact-verification/reply id @reply])
                                                           (rf/dispatch [:activity-center.notifications/mark-as-read id]))}
                                   {:label               (i18n/label :t/message-reply)
                                    :accessibility-label :send-reply-to-contact-verification
                                    :type                :primary
                                    :on-press            #(rf/dispatch [:bottom-sheet/show-sheet
                                                                        :activity-center.contact-verification/reply
                                                                        {:notification notification
                                                                         :replying?    true}])})})))])))))
