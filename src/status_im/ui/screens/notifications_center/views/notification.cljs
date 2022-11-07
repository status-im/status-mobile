(ns status-im.ui.screens.notifications-center.views.notification
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [quo.core :as quo]
            [quo.components.animated.pressable :as animation]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.screens.notifications-center.styles :as styles]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.constants :as constants]
            [status-im.activity-center.notification-types :as types]
            [quo.design-system.colors :as colors]
            [status-im.ui.screens.home.views.inner-item :as home-item]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.chat-icon.styles :as chat-icon.styles]))

(defn contact-request-actions [request-id]
  [react/view {:flex-direction :row}
   [animation/pressable {:on-press            #(re-frame/dispatch [:contact-requests.ui/accept-request request-id])
                         :accessibility-label :accept-cr}
    [icons/icon :main-icons/checkmark-circle {:width 35
                                              :height 35
                                              :color colors/green}]]
   [animation/pressable {:on-press            #(re-frame/dispatch [:contact-requests.ui/decline-request request-id])
                         :accessibility-label :decline-cr}
    [icons/icon :main-icons/cancel {:width 35
                                    :height 35
                                    :container-style {:margin-left 16}
                                    :color colors/red}]]])

(defn activity-text-item [home-item opts]
  (let [{:keys [chat-id chat-name message last-message reply-message muted read group-chat timestamp type color]} home-item
        message (or message last-message)
        {:keys [community-id]} (<sub [:chat-by-id chat-id])
        {:keys [name]} @(re-frame/subscribe [:communities/community community-id])
        contact (when message @(re-frame/subscribe [:contacts/contact-by-identity (message :from)]))
        title-text-width (* @(re-frame/subscribe [:dimensions/window-width]) 0.62)
        sender (when message (first @(re-frame/subscribe [:contacts/contact-two-names-by-identity (message :from)])))]
    [react/view
     [react/touchable-opacity (merge {:style (styles/notification-container read)} opts)
      [react/view {:style {:flex 1}}
       (when (or
              (= type types/contact-request)
              (= type types/contact-request-retracted))
         [react/view {:style {:padding-horizontal 20}}
          [quo/text {:weight :bold}
           (if
            (= type types/contact-request)
             (i18n/label :t/contact-request)
             (i18n/label :t/removed-from-contacts))]])
       (if (or
            (= type types/mention)
            (= type types/contact-request)
            (= type types/reply))
         [react/view {:style (styles/photo-container (= type types/contact-request))}

          [photos/photo
           (multiaccounts/displayed-photo contact)
           {:size 40
            :accessibility-label :current-account-photo}]]
         [chat-icon.screen/chat-icon-view chat-id group-chat chat-name
          {:container              (styles/photo-container false)
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
                  :style               (styles/title-text title-text-width)}
        (if (or
             (= type types/mention)
             (= type types/contact-request)
             (= type types/contact-request-retracted)
             (= type types/reply))
          sender
          [home-item/chat-item-title chat-id muted group-chat chat-name])]
       [react/text {:style               styles/datetime-text
                    :number-of-lines     1
                    :accessibility-label :notification-time-text}
       ;;TODO (perf) move to event
        (home-item/memo-timestamp timestamp)]
       [react/view {:style styles/notification-message-container}
        (when-not (= type types/contact-request-retracted)
          [home-item/message-content-text (select-keys message [:content :content-type :community-id]) false])
        (cond (= type types/mention)
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

              (= type types/reply)
              [react/view {:style styles/reply-message-container
                           :accessibility-label :reply-message-container}
               [icons/icon
                :main-icons/tiny-reply
                {:color  colors/gray
                 :width  18
                 :height 18
                 :container-style styles/reply-icon}]
               [home-item/message-content-text (select-keys reply-message [:content :content-type :community-id]) false]])]]]
     (when (= type types/contact-request)
       [react/view {:style {:margin-right 20
                            :margin-top 10
                            :align-self :flex-end}}
        (case (:contact-request-state message)
          constants/contact-request-message-state-accepted
          [quo/text {:style {:color colors/green}} (i18n/label :t/accepted)]
          constants/contact-request-message-state-declined
          [quo/text {:style {:color colors/red}} (i18n/label :t/declined)]
          constants/contact-request-message-state-pending
          [contact-request-actions (:message-id message)]
          nil)])]))
