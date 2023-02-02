(ns status-im.ui.screens.chat.sheets
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.message.sheets :as sheets.styles]
            [status-im.utils.universal-links.utils :as universal-links]))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn one-to-one-chat-accents
  [chat-id]
  (let [photo        @(re-frame/subscribe [:chats/photo-path chat-id])
        contact-name @(re-frame/subscribe [:contacts/contact-name-by-identity chat-id])]
    [react/view
     [quo/list-item
      {:theme               :accent
       :icon                [chat-icon/contact-icon-contacts-tab photo]
       :title               contact-name
       :subtitle            (i18n/label :t/view-profile)
       :accessibility-label :view-chat-details-button
       :chevron             true
       :on-press            #(do
                               (hide-sheet-and-dispatch [:chat.ui/show-profile chat-id])
                               (re-frame/dispatch [:pin-message/load-pin-messages chat-id]))}]
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/mark-all-read)
       :accessibility-label :mark-all-read-button
       :icon                :main-icons/check
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-pressed chat-id])}]
     [quo/list-item
      {:theme               :negative
       :title               (i18n/label :t/delete-chat)
       :accessibility-label :delete-chat-button
       :icon                :main-icons/delete
       :on-press            #(re-frame/dispatch [:chat.ui/show-remove-confirmation chat-id])}]]))

(defn public-chat-accents
  [chat-id]
  (let [link    (universal-links/generate-link :public-chat :external chat-id)
        message (i18n/label :t/share-public-chat-text {:link link})]
    [react/view
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/share-chat)
       :accessibility-label :share-chat-button
       :icon                :main-icons/share
       :on-press            (fn []
                              (re-frame/dispatch [:bottom-sheet/hide])
                              ;; https://github.com/facebook/react-native/pull/26839
                              (js/setTimeout
                               #(list-selection/open-share {:message message})
                               250))}]
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/mark-all-read)
       :accessibility-label :mark-all-read-button
       :icon                :main-icons/check
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-pressed chat-id])}]
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/clear-history)
       :accessibility-label :clear-history-button
       :icon                :main-icons/close
       :on-press            #(re-frame/dispatch [:chat.ui/show-clear-history-confirmation chat-id])}]
     [quo/list-item
      {:theme               :negative
       :title               (i18n/label :t/delete-chat)
       :accessibility-label :delete-chat-button
       :icon                :main-icons/delete
       :on-press            #(re-frame/dispatch [:chat.ui/show-remove-confirmation chat-id])}]]))

(defn community-chat-accents
  []
  (fn [{:keys [chat-id group-chat chat-name color emoji]}]
    [react/view
     [quo/list-item
      {:theme               :accent
       :title               chat-name
       :icon                [chat-icon/emoji-chat-icon-view-chat-sheet
                             chat-id group-chat chat-name color emoji]
       :subtitle            (i18n/label :t/view-details)
       :chevron             true
       :accessibility-label :view-community-channel-details
       :on-press            #(do
                               (hide-sheet-and-dispatch [:navigate-to :community-channel-details
                                                         {:chat-id chat-id}])
                               (re-frame/dispatch [:pin-message/load-pin-messages chat-id]))}]
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/mark-all-read)
       :accessibility-label :mark-all-read-button
       :icon                :main-icons/check
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-pressed chat-id])}]]))

(defn group-chat-accents
  []
  (fn [{:keys [chat-id group-chat chat-name color invitation-admin]}]
    (let [{:keys [member?]} @(re-frame/subscribe [:group-chat/inviter-info chat-id])
          removed?          @(re-frame/subscribe [:group-chat/removed-from-current-chat?])]
      (if invitation-admin
        [quo/list-item
         {:theme               :accent
          :title               (i18n/label :t/remove)
          :accessibility-label :remove-group-chat
          :icon                :main-icons/delete
          :on-press            #(hide-sheet-and-dispatch [:group-chats.ui/remove-chat-confirmed
                                                          chat-id])}]
        [react/view
         [quo/list-item
          {:theme    :accent
           :title    chat-name
           :subtitle (i18n/label :t/group-info)
           :icon     [chat-icon/chat-icon-view-chat-sheet
                      chat-id group-chat chat-name color]
           :chevron  true
           :on-press #(do
                        (hide-sheet-and-dispatch [:show-group-chat-profile chat-id])
                        (re-frame/dispatch [:pin-message/load-pin-messages chat-id]))}]
         [quo/list-item
          {:theme               :accent
           :title               (i18n/label :t/mark-all-read)
           :accessibility-label :mark-all-read-button
           :icon                :main-icons/check
           :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-pressed chat-id])}]
         (when member?
           [quo/list-item
            {:theme               :negative
             :title               (i18n/label :t/leave-chat)
             :accessibility-label :leave-chat-button
             :icon                :main-icons/arrow-left
             :on-press            #(re-frame/dispatch [:group-chats.ui/leave-chat-pressed chat-id])}])
         (when removed?
           [quo/list-item
            {:theme               :accent
             :title               (i18n/label :t/remove)
             :accessibility-label :remove-group-chat
             :icon                :main-icons/delete
             :on-press            #(hide-sheet-and-dispatch [:group-chats.ui/remove-chat-confirmed
                                                             chat-id])}])]))))

(defn actions
  [{:keys [chat-type chat-id]
    :as   current-chat}]
  (cond
    (#{constants/public-chat-type
       constants/profile-chat-type
       constants/timeline-chat-type}
     chat-type)
    [public-chat-accents chat-id]

    (= chat-type constants/community-chat-type)
    [community-chat-accents current-chat]

    (= chat-type constants/private-group-chat-type)
    [group-chat-accents current-chat]

    :else [one-to-one-chat-accents chat-id]))

(defn current-chat-actions
  []
  [actions @(re-frame/subscribe [:chats/current-chat])])

(defn chat-actions
  [chat-id]
  [actions @(re-frame/subscribe [:chat-by-id chat-id])])

(defn options
  [chat-id message-id]
  (fn []
    [react/view
     [react/i18n-text {:style sheets.styles/sheet-text :key :message-not-sent}]
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/resend-message)
       :icon                :main-icons/refresh
       :accessibility-label :resend-message-button
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/resend-message chat-id message-id])}]
     [quo/list-item
      {:theme               :negative
       :title               (i18n/label :t/delete-message)
       :icon                :main-icons/delete
       :accessibility-label :delete-transaccent-button
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/delete-message-not-used-any-more chat-id
                                                       message-id])}]]))
