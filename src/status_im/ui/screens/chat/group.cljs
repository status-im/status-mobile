(ns status-im.ui.screens.chat.group
  (:require [re-frame.core :as re-frame]
            [quo.core :as quo]
            [status-im.ui.components.react :as react]
            [status-im.constants :as constants]
            [status-im.utils.universal-links.utils :as links]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list-selection :as list-selection]
            [quo.design-system.colors :as colors]
            [status-im.utils.debounce :as debounce]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn decline-chat [chat-id]
  [quo/button
   {:type                :secondary
    :accessibility-label :decline-chat-button
    :on-press            #(debounce/dispatch-and-chill [:group-chats.ui/leave-chat-confirmed chat-id] 2000)}
   (i18n/label :t/group-chat-decline-invitation)])

(def message-max-length 100)

(defn request-membership [{:keys [state introduction-message] :as invitation}]
  (let [{:keys [message retry?]} @(re-frame/subscribe [:chats/current-chat-membership])
        message-length (count message)]
    [react/view {:margin-horizontal 16 :margin-top 10}
     (cond
       (and invitation (= constants/invitation-state-requested state) (not retry?))
       [react/view
        [react/text (i18n/label :t/introduce-yourself)]
        [react/text {:style {:margin-top         10 :margin-bottom 16 :min-height 66
                             :padding-horizontal 16 :padding-vertical 11
                             :border-color       colors/gray-lighter :border-width 1
                             :border-radius      8
                             :color              colors/gray}}
         introduction-message]
        [react/text {:style {:align-self :flex-end :margin-bottom 30
                             :color      colors/gray}}
         (str (count introduction-message) "/100")]]

       (and invitation (= constants/invitation-state-rejected state) (not retry?))
       [react/view
        [react/text {:style {:align-self :center :margin-bottom 30}}
         (i18n/label :t/membership-declined)]]

       :else
       [react/view
        [react/text (i18n/label :t/introduce-yourself)]
        [quo/text-input {:placeholder         (i18n/label :t/message)
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

(def group-chat-description-loading
  [react/view {:style (merge style/intro-header-description-container
                             {:margin-bottom 36
                              :height        44})}
   [react/text {:style style/intro-header-description}
    (i18n/label :t/loading)]
   [react/activity-indicator {:animating true
                              :size      :small
                              :color     colors/gray}]])

(defn calculate-quiet-time [synced-to
                            synced-from]
  (when synced-from
    (let [quiet-hours (quot (- synced-to synced-from)
                            (* 60 60))]
      (if (<= quiet-hours 24)
        (i18n/label :t/quiet-hours
                    {:quiet-hours quiet-hours})
        (i18n/label :t/quiet-days
                    {:quiet-days (quot quiet-hours 24)})))))

(defview no-messages-community-chat-description-container [chat-id]
  (letsubs [{:keys [synced-to synced-from]}
            [:chats/synced-to-and-from chat-id]]
    [react/text {:style (merge style/intro-header-description
                               {:margin-bottom 36})}
     (let [quiet-time (calculate-quiet-time synced-to
                                            synced-from)]
       (i18n/label :t/empty-chat-description-community
                   {:quiet-hours quiet-time}))]))

(defview no-messages-private-group-chat-description-container [chat-id]
  (letsubs [{:keys [synced-to synced-from]}
            [:chats/synced-to-and-from chat-id]]
    (let [quiet-time (calculate-quiet-time synced-to
                                           synced-from)]
      [react/nested-text {:style (merge style/intro-header-description
                                        {:margin-bottom 36})}
       (if quiet-time
         (i18n/label :t/empty-chat-description-public
                     {:quiet-hours quiet-time})
         (i18n/label :t/cleared-chat-description-public))
       [{:style    {:color colors/blue}
         :on-press #(list-selection/open-share
                     {:message
                      (i18n/label
                       :t/share-public-chat-text {:link (links/generate-link :public-chat :external chat-id)})})}
        (i18n/label :t/empty-chat-description-public-share-this)]])))

(defview pending-invitation-description
  [inviter-pk chat-name]
  (letsubs [inviter-name [:contacts/contact-name-by-identity inviter-pk]]
    [react/nested-text {:style style/intro-header-description}
     [{:style {:color colors/black}} inviter-name]
     (i18n/label :t/join-group-chat-description
                 {:username   ""
                  :group-name chat-name})]))

(defview joined-group-chat-description
  [inviter-pk chat-name]
  (letsubs [inviter-name [:contacts/contact-name-by-identity inviter-pk]]
    [react/nested-text {:style style/intro-header-description}
     (i18n/label :t/joined-group-chat-description
                 {:username   ""
                  :group-name chat-name})
     [{:style {:color colors/black}} inviter-name]]))

(defn created-group-chat-description [chat-name]
  [react/text {:style style/intro-header-description}
   (i18n/label :t/created-group-chat-description
               {:group-name chat-name})])

(defview group-chat-inviter-description-container [chat-id chat-name]
  (letsubs [{:keys [member? inviter-pk]}
            [:group-chat/inviter-info chat-id]]
    (cond
      (not member?)
      [pending-invitation-description inviter-pk chat-name]
      inviter-pk
      [joined-group-chat-description inviter-pk chat-name]
      :else
      [created-group-chat-description chat-name])))

(defn group-chat-membership-description []
  [react/text {:style {:text-align :center :margin-horizontal 30}}
   (i18n/label :t/membership-description)])

(defn group-chat-description-container
  [{:keys [invitation-admin
           chat-id
           chat-name
           chat-type
           loading-messages?
           no-messages?]}]
  (cond loading-messages?
        group-chat-description-loading

        (and no-messages? (= chat-type constants/public-chat-type))
        [no-messages-private-group-chat-description-container chat-id]

        (and no-messages? (= chat-type constants/community-chat-type))
        [no-messages-community-chat-description-container chat-id]

        invitation-admin
        [group-chat-membership-description]

        (= chat-type constants/private-group-chat-type)
        [group-chat-inviter-description-container chat-id chat-name]))
