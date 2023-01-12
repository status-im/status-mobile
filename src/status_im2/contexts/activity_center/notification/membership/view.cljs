(ns status-im2.contexts.activity-center.notification.membership.view
  (:require [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [id accepted author read timestamp chat-name]}]
  [quo/activity-log
   (merge
    {:title     (i18n/label :t/added-to-group-chat)
     :icon      :i/add-user
     :timestamp (datetime/timestamp->relative timestamp)
     :unread?   (not read)
     :context   [[common/user-avatar-tag author]
                 (i18n/label :t/added-you-to)
                 [quo/group-avatar-tag chat-name
                  {:size  :small
                   :color :purple}]]}
    (when-not accepted
      {:button-2 {:label               (i18n/label :t/accept)
                  :accessibility-label :accept-group-chat-invitation
                  :type                :positive
                  :on-press            #(rf/dispatch [:activity-center.notifications/accept id])}
       :button-1 {:label               (i18n/label :t/decline)
                  :accessibility-label :decline-group-chat-invitation
                  :type                :danger
                  :on-press            #(rf/dispatch [:activity-center.notifications/dismiss id])}}))])
