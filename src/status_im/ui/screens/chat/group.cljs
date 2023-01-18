(ns status-im.ui.screens.chat.group
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im2.setup.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [utils.debounce :as debounce])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn decline-chat
  [chat-id]
  [quo/button
   {:type                :secondary
    :accessibility-label :decline-chat-button
    :on-press            #(debounce/dispatch-and-chill [:group-chats.ui/leave-chat-confirmed chat-id]
                                                       2000)}
   (i18n/label :t/group-chat-decline-invitation)])

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
