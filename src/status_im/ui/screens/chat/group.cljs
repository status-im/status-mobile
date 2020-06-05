(ns status-im.ui.screens.chat.group
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.react :as react]
            [status-im.utils.universal-links.core :as links]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn join-chat-button [chat-id]
  [button/button
   {:type     :secondary
    :on-press #(re-frame/dispatch [:group-chats.ui/join-pressed chat-id])
    :label    :t/join-group-chat}])

(defn decline-chat [chat-id]
  [react/touchable-highlight
   {:on-press
    #(re-frame/dispatch [:group-chats.ui/leave-chat-confirmed chat-id])}
   [react/text {:style style/decline-chat}
    (i18n/label :t/group-chat-decline-invitation)]])

(defview group-chat-footer
  [chat-id]
  (letsubs [{:keys [joined?]} [:group-chat/inviter-info chat-id]]
    (when-not joined?
      [react/view {:style style/group-chat-join-footer}
       [react/view {:style style/group-chat-join-container}
        [join-chat-button chat-id]
        [decline-chat chat-id]]])))

(def group-chat-description-loading
  [react/view {:style (merge style/intro-header-description-container
                             {:margin-bottom 36
                              :height        44})}
   [react/text {:style style/intro-header-description}
    (i18n/label :t/loading)]
   [react/activity-indicator {:animating true
                              :size      :small
                              :color     colors/gray}]])

(defview no-messages-group-chat-description-container [chat-id]
  (letsubs [{:keys [highest-request-to lowest-request-from]}
            [:mailserver/ranges-by-chat-id chat-id]]
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
                     :t/share-public-chat-text {:link (links/generate-link :public-chat :external chat-id)})})}
      (i18n/label :t/empty-chat-description-public-share-this)]]))

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
  (letsubs [{:keys [joined? inviter-pk]}
            [:group-chat/inviter-info chat-id]]
    (cond
      (not joined?)
      [pending-invitation-description inviter-pk chat-name]
      inviter-pk
      [joined-group-chat-description inviter-pk chat-name]
      :else
      [created-group-chat-description chat-name])))

(defn group-chat-description-container
  [{:keys [public?
           chat-id
           chat-name
           loading-messages?
           no-messages?]}]
  (cond loading-messages?
        group-chat-description-loading

        (and no-messages? public?)
        [no-messages-group-chat-description-container chat-id]

        (not public?)
        [group-chat-inviter-description-container chat-id chat-name]))
