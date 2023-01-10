(ns status-im2.contexts.activity-center.notification.admin.view
  (:require [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [status-im.constants :as constants]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [status-im2.contexts.activity-center.notification.reply.style :as style]
            [utils.datetime :as datetime]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [author communityId id membershipStatus read timestamp]}]
  (let [community               (rf/sub [:communities/community communityId])
        community-name          (:name community)
        community-image         (get-in community [:images :thumbnail :uri])]
     [quo/activity-log
      (merge 
       {:title     (i18n/label :t/join-request)
       :icon      :i/add-user
       :timestamp (datetime/timestamp->relative timestamp)
       :unread?   (not read)
       :context   [[common/user-avatar-tag author]
                   [quo/text {:style style/lowercase-text} (i18n/label :t/wants-to-join)]
                   [quo/context-tag
                    {:size           :small
                     :override-theme :dark
                     :color          colors/primary-50
                     :style          style/tag
                     :text-style     style/tag-text}
                    {:uri community-image} community-name]]
        :status    (case membershipStatus
                    constants/membership-status-accepted
                     {:type :positive :label (i18n/label :t/accepted)}
                     constants/membership-status-declined
                     {:type :negative :label (i18n/label :t/declined)}
                     nil)}
       (case membershipStatus
          constants/membership-status-pending
          {:button-1 {:label               (i18n/label :t/decline)
                      :accessibility-label :decline-contact-request
                      :type                :danger
                      :on-press            (fn []
                                             (rf/dispatch [:communities.ui/decline-request-to-join-pressed communityId id])
                                             (rf/dispatch [:activity-center.notifications/mark-as-read id]))}
           :button-2 {:label               (i18n/label :t/accept)
                      :accessibility-label :accept-contact-request
                      :type                :positive
                      :on-press            (fn []
                                             (rf/dispatch [:communities.ui/accept-request-to-join-pressed communityId id])
                                             (rf/dispatch [:activity-center.notifications/mark-as-read id]))}}
          nil))]))
