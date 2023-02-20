(ns status-im2.contexts.activity-center.notification.admin.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [status-im2.constants :as constants]
            [status-im2.contexts.activity-center.notification.common.style :as style]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [author community-id id membership-status read timestamp]}]
  (let [community       (rf/sub [:communities/community community-id])
        community-name  (:name community)
        community-image (get-in community [:images :thumbnail :uri])]
    [quo/activity-log
     {:title     (i18n/label :t/join-request)
      :icon      :i/add-user
      :timestamp (datetime/timestamp->relative timestamp)
      :unread?   (not read)
      :context   [[common/user-avatar-tag author]
                  (i18n/label :t/wants-to-join)
                  [quo/context-tag
                   {:size           :small
                    :override-theme :dark
                    :color          colors/primary-50
                    :style          style/user-avatar-tag
                    :text-style     style/user-avatar-tag-text}
                   {:uri community-image} community-name]]
      :items     (case membership-status
                   constants/activity-center-membership-status-accepted
                   [{:type    :status
                     :subtype :positive
                     :key     :status-accepted
                     :label   (i18n/label :t/accepted)}]

                   constants/activity-center-membership-status-declined
                   [{:type    :status
                     :subtype :negative
                     :key     :status-declined
                     :label   (i18n/label :t/declined)}]

                   constants/activity-center-membership-status-pending
                   [{:type                :button
                     :subtype             :danger
                     :key                 :button-decline
                     :label               (i18n/label :t/decline)
                     :accessibility-label :decline-join-request
                     :on-press            (fn []
                                            (rf/dispatch [:communities.ui/decline-request-to-join-pressed
                                                          community-id id]))}
                    {:type                :button
                     :subtype             :positive
                     :key                 :button-accept
                     :label               (i18n/label :t/accept)
                     :accessibility-label :accept-join-request
                     :on-press            (fn []
                                            (rf/dispatch [:communities.ui/accept-request-to-join-pressed
                                                          community-id id]))}]

                   nil)}]))
