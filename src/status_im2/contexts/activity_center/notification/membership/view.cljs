(ns status-im2.contexts.activity-center.notification.membership.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn pressable
  [{:keys [accepted chat-id]} & children]
  (if accepted
    (into [rn/touchable-opacity
           {:on-press (fn []
                        (rf/dispatch [:hide-popover])
                        (rf/dispatch [:chat/navigate-to-chat chat-id]))}]
          children)
    (into [:<>] children)))

(defn view
  [{:keys [id accepted author read timestamp chat-name chat-id]}]
  [pressable {:accepted accepted :chat-id chat-id}
   [quo/activity-log
    {:title     (i18n/label :t/added-to-group-chat)
     :icon      :i/add-user
     :timestamp (datetime/timestamp->relative timestamp)
     :unread?   (not read)
     :context   [[common/user-avatar-tag author]
                 (i18n/label :t/added-you-to)
                 [quo/group-avatar-tag chat-name
                  {:size  :small
                   :color :purple}]]
     :items     (when-not accepted
                  [{:type                :button
                    :subtype             :positive
                    :label               (i18n/label :t/accept)
                    :accessibility-label :accept-group-chat-invitation
                    :on-press            #(rf/dispatch [:activity-center.notifications/accept id])}
                   {:type                :button
                    :subtype             :danger
                    :label               (i18n/label :t/decline)
                    :accessibility-label :decline-group-chat-invitation
                    :on-press            #(rf/dispatch [:activity-center.notifications/dismiss
                                                        id])}])}]])
