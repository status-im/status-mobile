(ns status-im.ui.screens.notifications-center.views.notification
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [quo.core :as quo]
            [status-im.ui.screens.notifications-center.styles :as styles]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.constants :as constants]
            [quo.design-system.colors :as colors]
            [status-im.ui.screens.home.views.inner-item :as home-item]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.chat-icon.styles :as chat-icon.styles]))

(defn activity-text-item [home-item opts]
  (let [{:keys [chat-id chat-name message last-message reply-message muted read group-chat timestamp type color]} home-item
        message (or message last-message)
        {:keys [community-id]} (<sub [:chat-by-id chat-id])
        {:keys [name]} @(re-frame/subscribe [:communities/community community-id])
        contact (when message @(re-frame/subscribe [:contacts/contact-by-identity (message :from)]))
        sender (when message (first @(re-frame/subscribe [:contacts/contact-two-names-by-identity (message :from)])))]
    [react/touchable-opacity (merge {:style (styles/notification-container read)} opts)
     [react/view {:style styles/notification-content-container}
      (if (or
           (= type constants/activity-center-notification-type-mention)
           (= type constants/activity-center-notification-type-reply))
        [react/view {:style styles/photo-container}
         [photos/photo
          (multiaccounts/displayed-photo contact)
          {:size 40
           :accessibility-label :current-account-photo}]]
        [chat-icon.screen/chat-icon-view chat-id group-chat chat-name
         {:container              styles/photo-container
          :size                   40
          :chat-icon              chat-icon.styles/chat-icon-chat-list
          :default-chat-icon      (chat-icon.styles/default-chat-icon-chat-list color)
          :default-chat-icon-text (chat-icon.styles/default-chat-icon-text 40)
          :accessibility-label    :current-account-photo}])
      [quo/text {:weight              :medium
                 :color               (when muted :secondary)
                 :accessibility-label :chat-name-or-sender-text
                 :ellipsize-mode      :tail
                 :number-of-lines     1
                 :style               styles/title-text}
       (if (or
            (= type constants/activity-center-notification-type-mention)
            (= type constants/activity-center-notification-type-reply))
         sender
         [home-item/chat-item-title chat-id muted group-chat chat-name])]
      [react/text {:style               styles/datetime-text
                   :number-of-lines     1
                   :accessibility-label :notification-time-text}
       ;;TODO (perf) move to event
       (home-item/memo-timestamp timestamp)]
      [react/view {:style styles/notification-message-container}
       [home-item/message-content-text (select-keys message [:content :content-type :community-id]) false]
       (cond (= type constants/activity-center-notification-type-mention)
             [react/view {:style styles/group-info-container
                          :accessibility-label :chat-name-container}
              [icons/icon
               (if community-id :main-icons/tiny-community :main-icons/tiny-group)
               {:color  colors/gray
                :width  16
                :height 16
                :container-style styles/group-icon}]
              (when community-id
                [react/view {:style styles/community-info-container}
                 [quo/text {:color :secondary
                            :weight :medium
                            :size :small}
                  name]
                 [icons/icon
                  :main-icons/chevron-right
                  {:color  colors/gray
                   :width  16
                   :height 22}]])
              [quo/text {:color :secondary
                         :weight :medium
                         :size :small}
               (str (when community-id "#") chat-name)]]

             (= type constants/activity-center-notification-type-reply)
             [react/view {:style styles/reply-message-container
                          :accessibility-label :reply-message-container}
              [icons/icon
               :main-icons/tiny-reply
               {:color  colors/gray
                :width  18
                :height 18
                :container-style styles/reply-icon}]
              [home-item/message-content-text (select-keys reply-message [:content :content-type :community-id]) false]])]]]))
