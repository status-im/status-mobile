(ns status-im.ui.screens.chat.sheets
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.constants :as constants]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.universal-links.utils :as universal-links]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.chat.styles.message.sheets :as sheets.styles]
            [quo.core :as quo]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn one-to-one-chat-accents [{:keys [chat-id]}]
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
       :on-press            #(hide-sheet-and-dispatch  [:chat.ui/show-profile chat-id])}]
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
       :on-press            #(re-frame/dispatch [:chat.ui/clear-history-pressed chat-id])}]
     [quo/list-item
      {:theme               :negative
       :title               (i18n/label :t/delete-chat)
       :accessibility-label :delete-chat-button
       :icon                :main-icons/delete
       :on-press            #(re-frame/dispatch [:chat.ui/remove-chat-pressed chat-id])}]]))

(defn public-chat-accents [{:keys [chat-id]}]
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
       :on-press            #(re-frame/dispatch [:chat.ui/clear-history-pressed chat-id])}]
     [quo/list-item
      {:theme               :negative
       :title               (i18n/label :t/delete-chat)
       :accessibility-label :delete-chat-button
       :icon                :main-icons/delete
       :on-press            #(re-frame/dispatch [:chat.ui/remove-chat-pressed chat-id])}]]))

(defn community-chat-accents []
  (fn [{:keys [chat-id group-chat chat-name color]}]
    [react/view
     [quo/list-item
      {:theme    :accent
       :title    chat-name
       :icon     [chat-icon/chat-icon-view-chat-sheet
                  chat-id group-chat chat-name color]}]
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
       :on-press            #(re-frame/dispatch [:chat.ui/clear-history-pressed chat-id])}]]))

(defn group-chat-accents []
  (fn [{:keys [chat-id group-chat chat-name color invitation-admin]}]
    (let [{:keys [joined?]} @(re-frame/subscribe [:group-chat/inviter-info chat-id])
          removed? @(re-frame/subscribe [:group-chat/removed-from-current-chat?])]
      (if invitation-admin
        [quo/list-item
         {:theme               :accent
          :title               (i18n/label :t/remove)
          :accessibility-label :remove-group-chat
          :icon                :main-icons/delete
          :on-press            #(hide-sheet-and-dispatch [:group-chats.ui/remove-chat-confirmed chat-id])}]
        [react/view
         [quo/list-item
          {:theme    :accent
           :title    chat-name
           :subtitle (i18n/label :t/group-info)
           :icon     [chat-icon/chat-icon-view-chat-sheet
                      chat-id group-chat chat-name color]
           :chevron  true
           :on-press #(hide-sheet-and-dispatch [:show-group-chat-profile chat-id])}]
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
           :on-press            #(re-frame/dispatch [:chat.ui/clear-history-pressed chat-id])}]
         (when joined?
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
             :on-press            #(hide-sheet-and-dispatch [:group-chats.ui/remove-chat-confirmed chat-id])}])]))))

(defn actions [{:keys [chat-type]
                :as current-chat}]
  (cond
    (#{constants/public-chat-type
       constants/profile-chat-type
       constants/timeline-chat-type} chat-type)
    [public-chat-accents current-chat]

    (= chat-type constants/community-chat-type)
    [community-chat-accents current-chat]

    (= chat-type constants/private-group-chat-type)
    [group-chat-accents current-chat]

    :else      [one-to-one-chat-accents current-chat]))

(defn options [chat-id message-id]
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
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/delete-message chat-id message-id])}]]))

(defn image-long-press [{:keys [content identicon from outgoing cant-be-replied] :as message} hide]
  (let [contact-name @(re-frame/subscribe [:contacts/contact-name-by-identity from])]
    [react/view
     (when-not outgoing
       [quo/list-item
        {:theme               :accent
         :icon                [chat-icon/contact-icon-contacts-tab
                               (multiaccounts/displayed-photo {:identicon  identicon
                                                               :public-key from})]
         :title               contact-name
         :subtitle            (i18n/label :t/view-profile)
         :accessibility-label :view-chat-details-button
         :chevron             true
         :on-press            #(do
                                 (hide)
                                 (re-frame/dispatch [:chat.ui/show-profile from]))}])
     (when-not cant-be-replied
       [quo/list-item
        {:theme    :accent
         :title    (i18n/label :t/message-reply)
         :icon     :main-icons/reply
         :on-press #(do
                      (hide)
                      (re-frame/dispatch [:chat.ui/reply-to-message message]))}])
     ;; we have only base64 string for image, so we need to find a way how to copy it
     #_[quo/list-item
        {:theme    :accent
         :title    :t/sharing-copy-to-clipboard
         :icon     :main-icons/copy
         :on-press (fn []
                     (re-frame/dispatch [:bottom-sheet/hide])
                     (react/copy-to-clipboard (:image content)))}]
     [quo/list-item
      {:theme    :accent
       :title    (i18n/label :t/save)
       :icon     :main-icons/download
       :on-press (fn []
                   (hide)
                   (re-frame/dispatch [:chat.ui/save-image-to-gallery (:image content)]))}]
     ;; we have only base64 string for image, so we need to find a way how to share it
     #_[quo/list-item
        {:theme    :accent
         :title    :t/sharing-share
         :icon     :main-icons/share
         :on-press (fn []
                     (re-frame/dispatch [:bottom-sheet/hide])
                     (list-selection/open-share {:message (:image content)}))}]]))
