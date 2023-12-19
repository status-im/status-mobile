(ns legacy.status-im.ui.screens.chat.group
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [react-native.platform :as platform]
    [status-im2.constants :as constants]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]]))

(def message-max-length 100)

(defn request-membership
  [{:keys [state introduction-message] :as invitation}]
  (let [{:keys [message retry?]} @(re-frame/subscribe [:chats/current-chat-membership])
        message-length           (count message)]
    [react/view {:margin-horizontal 16 :margin-top 10}
     (cond
       (and invitation (= constants/invitation-state-requested state) (not retry?))
       [react/view
        [react/text (i18n/label :t/introduce-yourself)]
        [react/text
         {:style {:margin-top         10
                  :margin-bottom      16
                  :min-height         66
                  :padding-horizontal 16
                  :padding-vertical   11
                  :border-color       colors/gray-lighter
                  :border-width       1
                  :border-radius      8
                  :color              colors/gray}}
         introduction-message]
        [react/text
         {:style {:align-self    :flex-end
                  :margin-bottom 30
                  :color         colors/gray}}
         (str (count introduction-message) "/100")]]

       (and invitation (= constants/invitation-state-rejected state) (not retry?))
       [react/view
        [react/text {:style {:align-self :center :margin-bottom 30}}
         (i18n/label :t/membership-declined)]]

       :else
       [react/view
        [react/text (i18n/label :t/introduce-yourself)]
        [quo/text-input
         {:placeholder         (i18n/label :t/message)
          :on-change-text      #(re-frame/dispatch [:group-chats.ui/update-membership-message %])
          :max-length          (if platform/android?
                                 message-max-length
                                 (when (>= message-length message-max-length)
                                   message-length))
          :multiline           true
          :default-value       message
          :accessibility-label :introduce-yourself-input
          :container-style     {:margin-top 10 :margin-bottom 16}}]
        [react/text {:style {:align-self :flex-end :margin-bottom 30}}
         (str (count message) "/100")]])]))

(defview group-chat-footer
  [chat-id invitation-admin]
  (letsubs [invitations [:group-chat/invitations-by-chat-id chat-id]]
    (when invitation-admin
      [request-membership (first invitations)])))
