(ns status-im.contexts.shell.activity-center.notification.admin.view
  (:require
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
  [{:keys [notification extra-fn]} child]
  (let [{:keys [community-id id
                membership-status]} notification
        accept                      (rn/use-callback
                                     (fn []
                                       (rf/dispatch [:communities/accept-request-to-join-pressed
                                                     community-id id]))
                                     [community-id id])
        decline                     (rn/use-callback
                                     (fn []
                                       (rf/dispatch [:communities/decline-request-to-join-pressed
                                                     community-id id]))
                                     [community-id id])]
    (cond
      (#{constants/activity-center-membership-status-accepted
         constants/activity-center-membership-status-declined}
       membership-status)
      [common/swipeable
       {:left-button    common/swipe-button-read-or-unread
        :left-on-press  common/swipe-on-press-toggle-read
        :right-button   common/swipe-button-delete
        :right-on-press common/swipe-on-press-delete
        :extra-fn       extra-fn}
       child]

      (= membership-status constants/activity-center-membership-status-pending)
      [common/swipeable
       {:left-button    swipe-button-accept
        :left-on-press  accept
        :right-button   swipe-button-decline
        :right-on-press decline
        :extra-fn       extra-fn}
       child]

      :else
      child)))

(defn view
  [{:keys [notification] :as props}]
  (let [{:keys [author community-id id membership-status
                read timestamp]} notification
        community-name           (rf/sub [:communities/name community-id])
        community-logo           (rf/sub [:communities/logo community-id])
        customization-color      (rf/sub [:profile/customization-color])]
    [swipeable props
     [quo/activity-log
      {:title               (i18n/label :t/join-request)
       :customization-color customization-color
       :icon                :i/add-user
       :timestamp           (datetime/timestamp->relative timestamp)
       :unread?             (not read)
       :context             [[common/user-avatar-tag author]
                             (i18n/label :t/wants-to-join)
                             [quo/context-tag
                              {:type           :community
                               :size           24
                               :blur?          true
                               :community-logo community-logo
                               :community-name community-name}]]
       :items               (condp = membership-status
                              constants/activity-center-membership-status-accepted
                              [{:type    :status
                                :subtype :positive
                                :key     :status-accepted
                                :blur?   true
                                :label   (i18n/label :t/accepted)}]

                              constants/activity-center-membership-status-declined
                              [{:type    :status
                                :subtype :negative
                                :key     :status-declined
                                :blur?   true
                                :label   (i18n/label :t/declined)}]

                              constants/activity-center-membership-status-pending
                              [{:type                :button
                                :subtype             :danger
                                :key                 :button-decline
                                :label               (i18n/label :t/decline)
                                :accessibility-label :decline-join-request
                                :on-press            (fn []
                                                       (rf/dispatch
                                                        [:communities/decline-request-to-join-pressed
                                                         community-id id]))}
                               {:type                :button
                                :subtype             :positive
                                :key                 :button-accept
                                :label               (i18n/label :t/accept)
                                :accessibility-label :accept-join-request
                                :on-press            (fn []
                                                       (rf/dispatch
                                                        [:communities/accept-request-to-join-pressed
                                                         community-id id]))}]

                              nil)}]]))
