(ns status-im.ui.screens.chat.group
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.colors :as colors]))

(defn join-chat-button [chat-id]
  [button/button
   {:type     :secondary
    :on-press #(re-frame/dispatch [:group-chats.ui/join-pressed chat-id])
    :label    :t/join-group-chat}])

(defn decline-chat [chat-id]
  [react/touchable-highlight
   {:on-press
    #(re-frame/dispatch [:group-chats.ui/remove-chat-confirmed chat-id])}
   [react/text {:style style/decline-chat}
    (i18n/label :t/group-chat-decline-invitation)]])

(defn group-chat-footer
  [chat-id]
  [react/view {:style style/group-chat-join-footer}
   [react/view {:style style/group-chat-join-container}
    [join-chat-button chat-id]
    [decline-chat chat-id]]])

(defn group-chat-description-loading
  []
  [react/view {:style (merge style/intro-header-description-container
                             {:margin-bottom 36
                              :height        44})}
   [react/text {:style style/intro-header-description}
    (i18n/label :t/loading)]
   [react/activity-indicator {:animating true
                              :size      :small
                              :color     colors/gray}]])

(defn group-chat-description-container
  [{:keys [pending-invite-inviter-name inviter-name chat-name public?
           universal-link range intro-status]}]
  (let [{:keys [lowest-request-from highest-request-to]} range]
    (case intro-status
      :loading
      [group-chat-description-loading]

      :empty
      (when public?
        [react/nested-text {:style (merge style/intro-header-description
                                          {:margin-bottom 36})}
         (let [quiet-hours (quot (- highest-request-to lowest-request-from)
                                 (* 60 60))
               quiet-time  (if (<= quiet-hours 24)
                             (i18n/label :t/quiet-hours
                                         {:quiet-hours quiet-hours})
                             (i18n/label :t/quiet-days
                                         {:quiet-days (quot quiet-hours 24)}))]
           (i18n/label :t/empty-chat-description-public
                       {:quiet-hours quiet-time}))
         [{:style    {:color colors/blue}
           :on-press #(list-selection/open-share
                       {:message
                        (i18n/label
                         :t/share-public-chat-text {:link universal-link})})}
          (i18n/label :t/empty-chat-description-public-share-this)]])

      :messages
      (when (not public?)
        (if pending-invite-inviter-name
          [react/nested-text {:style style/intro-header-description}
           [{:style {:color colors/black}} pending-invite-inviter-name]
           (i18n/label :t/join-group-chat-description
                       {:username   ""
                        :group-name chat-name})]
          (if (not= inviter-name "Unknown")
            [react/nested-text {:style style/intro-header-description}
             (i18n/label :t/joined-group-chat-description
                         {:username   ""
                          :group-name chat-name})
             [{:style {:color colors/black}} inviter-name]]
            [react/text {:style style/intro-header-description}
             (i18n/label :t/created-group-chat-description
                         {:group-name chat-name})]))))))