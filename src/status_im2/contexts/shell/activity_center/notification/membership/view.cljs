(ns status-im2.contexts.shell.activity-center.notification.membership.view
  (:require [quo2.core :as quo]
            [react-native.gesture :as gesture]
            [status-im2.contexts.shell.activity-center.notification.common.style :as common-style]
            [status-im2.contexts.shell.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- pressable
  [{:keys [accepted chat-id]} child]
  (if accepted
    [gesture/touchable-without-feedback
     {:on-press (fn []
                  (rf/dispatch [:hide-popover])
                  (rf/dispatch [:chat/navigate-to-chat chat-id]))}
     child]
    child))

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
  [{:keys [active-swipeable notification extra-fn]} child]
  (let [{:keys [accepted dismissed id]} notification]
    (if (or accepted dismissed)
      [common/swipeable
       {:left-button      common/swipe-button-read-or-unread
        :left-on-press    common/swipe-on-press-toggle-read
        :right-button     common/swipe-button-delete
        :right-on-press   common/swipe-on-press-delete
        :active-swipeable active-swipeable
        :extra-fn         extra-fn}
       child]
      [common/swipeable
       {:left-button      swipe-button-accept
        :left-on-press    #(rf/dispatch [:activity-center.notifications/accept id])
        :right-button     swipe-button-decline
        :right-on-press   #(rf/dispatch [:activity-center.notifications/dismiss id])
        :active-swipeable active-swipeable
        :extra-fn         extra-fn}
       child])))

(defn view
  [{:keys [notification set-swipeable-height customization-color] :as props}]
  (let [{:keys [id accepted dismissed author read timestamp chat-name chat-id]} notification]
    [swipeable props
     [pressable {:accepted accepted :chat-id chat-id}
      [quo/activity-log
       {:title               (i18n/label :t/added-to-group-chat)
        :customization-color customization-color
        :on-layout           set-swipeable-height
        :icon                :i/add-user
        :timestamp           (datetime/timestamp->relative timestamp)
        :unread?             (not read)
        :context             [[common/user-avatar-tag author]
                              (i18n/label :t/added-you-to)
                              [quo/group-avatar-tag chat-name
                               {:size  :x-small
                                :customization-color :purple}]]
        :items               (when-not (or accepted dismissed)
                               [{:type                :button
                                 :subtype             :positive
                                 :key                 :button-accept
                                 :label               (i18n/label :t/accept)
                                 :accessibility-label :accept-group-chat-invitation
                                 :on-press            #(rf/dispatch
                                                        [:activity-center.notifications/accept id])}
                                {:type                :button
                                 :subtype             :danger
                                 :key                 :button-decline
                                 :label               (i18n/label :t/decline)
                                 :accessibility-label :decline-group-chat-invitation
                                 :on-press            #(rf/dispatch
                                                        [:activity-center.notifications/dismiss
                                                         id])}])}]]]))
