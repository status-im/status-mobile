(ns status-im.ui.screens.chat.sheets
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.colors :as colors]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.chat.styles.message.sheets :as sheets.styles]
            [status-im.ui.components.list-item.views :as list-item]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn view-profile [{:keys [name helper]}]
  [react/view
   [react/text {:style {:font-weight "500"
                        :line-height 22
                        :font-size   15
                        :color       colors/black}}
    name]
   [react/text {:style {:line-height 22
                        :font-size   15
                        :color       colors/gray}}
    (i18n/label helper)]])

(defn one-to-one-chat-actions [{:keys [chat-id]}]
  (let [photo @(re-frame/subscribe [:chats/photo-path chat-id])
        contact-name @(re-frame/subscribe [:contacts/contact-name-by-identity chat-id])]
    [react/view
     [list-item/list-item
      {:theme       :action
       :icon        photo
       :title       [view-profile {:name   contact-name
                                   :helper :t/view-profile}]
       :accessibility-label :view-chat-details-button
       :accessories [:chevron]
       :on-press    #(hide-sheet-and-dispatch  [:chat.ui/show-profile chat-id])}]
     [list-item/list-item
      {:theme    :action
       :title    :t/mark-all-read
       :accessibility-label :mark-all-read-button
       :icon     :main-icons/check
       :on-press #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-pressed chat-id])}]
     [list-item/list-item
      {:theme    :action
       :title    :t/clear-history
       :accessibility-label :clear-history-button
       :icon     :main-icons/close
       :on-press #(hide-sheet-and-dispatch [:chat.ui/clear-history-pressed chat-id])}]
     [list-item/list-item
      {:theme    :action
       :title    :t/fetch-history
       :accessibility-label :fetch-history-button
       :icon     :main-icons/arrow-down
       :on-press #(hide-sheet-and-dispatch [:chat.ui/fetch-history-pressed chat-id])}]
     [list-item/list-item
      {:theme    :action-destructive
       :title    :t/delete-chat
       :accessibility-label :delete-chat-button
       :icon     :main-icons/delete
       :on-press #(hide-sheet-and-dispatch [:chat.ui/remove-chat-pressed chat-id])}]]))

(defn public-chat-actions [{:keys [chat-id]}]
  (let [link    (universal-links/generate-link :public-chat :external chat-id)
        message (i18n/label :t/share-public-chat-text {:link link})]
    [react/view
     (when-not platform/desktop?
       [list-item/list-item
        {:theme    :action
         :title    :t/share-chat
         :accessibility-label :share-chat-button
         :icon     :main-icons/share
         :on-press (fn []
                     (re-frame/dispatch [:bottom-sheet/hide-sheet])
                     (list-selection/open-share {:message message}))}])
     [list-item/list-item
      {:theme    :action
       :title    :t/mark-all-read
       :accessibility-label :mark-all-read-button
       :icon     :main-icons/check
       :on-press #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-pressed chat-id])}]
     [list-item/list-item
      {:theme    :action
       :title    :t/clear-history
       :accessibility-label :clear-history-button
       :icon     :main-icons/close
       :on-press #(hide-sheet-and-dispatch [:chat.ui/clear-history-pressed chat-id])}]
     [list-item/list-item
      {:theme    :action
       :title    :t/fetch-history
       :accessibility-label :fetch-history-button
       :icon     :main-icons/arrow-down
       :on-press #(hide-sheet-and-dispatch [:chat.ui/fetch-history-pressed chat-id])}]
     [list-item/list-item
      {:theme    :action-destructive
       :title    :t/delete-chat
       :accessibility-label :delete-chat-button
       :icon     :main-icons/delete
       :on-press #(hide-sheet-and-dispatch [:chat.ui/remove-chat-pressed chat-id])}]]))

(defn group-chat-actions []
  (fn [{:keys [chat-id group-chat chat-name color]}]
    (let [{:keys [joined?]} @(re-frame/subscribe [:group-chat/inviter-info chat-id])]
      [react/view
       [list-item/list-item
        {:theme       :action
         :title       [view-profile {:name   chat-name
                                     :helper :t/group-info}]
         :icon        [chat-icon/chat-icon-view-chat-sheet
                       chat-id group-chat chat-name color]
         :accessories [:chevron]
         :on-press    #(hide-sheet-and-dispatch [:show-group-chat-profile chat-id])}]
       [list-item/list-item
        {:theme               :action
         :title               :t/mark-all-read
         :accessibility-label :mark-all-read-button
         :icon                :main-icons/check
         :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-pressed chat-id])}]
       [list-item/list-item
        {:theme               :action
         :title               :t/clear-history
         :accessibility-label :clear-history-button
         :icon                :main-icons/close
         :on-press            #(hide-sheet-and-dispatch [:chat.ui/clear-history-pressed chat-id])}]
       [list-item/list-item
        {:theme               :action
         :title               :t/fetch-history
         :accessibility-label :fetch-history-button
         :icon                :main-icons/arrow-down
         :on-press            #(hide-sheet-and-dispatch [:chat.ui/fetch-history-pressed chat-id])}]
       (when joined?
         [list-item/list-item
          {:theme               :action
           :title               :t/leave-chat
           :accessibility-label :leave-chat-button
           :icon                :main-icons/arrow-left
           :on-press            #(hide-sheet-and-dispatch [:group-chats.ui/leave-chat-pressed chat-id])}])])))

(defn actions [{:keys [public? group-chat]
                :as current-chat}]
  (cond
    public?    [public-chat-actions current-chat]
    group-chat [group-chat-actions current-chat]
    :else      [one-to-one-chat-actions current-chat]))

(defn options [chat-id message-id]
  (fn []
    [react/view
     [react/i18n-text {:style sheets.styles/sheet-text :key :message-not-sent}]
     [list-item/list-item
      {:theme               :action
       :title               :t/resend-message
       :icon                :main-icons/refresh
       :accessibility-label :resend-message-button
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/resend-message chat-id message-id])}]
     [list-item/list-item
      {:theme               :action-destructive
       :title               :t/delete-message
       :icon                :main-icons/delete
       :accessibility-label :delete-transaction-button
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/delete-message chat-id message-id])}]]))

(defn message-long-press [{:keys [content from outgoing] :as message}]
  (fn []
    (let [photo @(re-frame/subscribe [:chats/photo-path from])
          contact-name @(re-frame/subscribe [:contacts/contact-name-by-identity from])]
      [react/view
       (when-not outgoing
         [list-item/list-item
          {:theme               :action
           :icon                photo
           :title               [view-profile {:name   contact-name
                                               :helper :t/view-profile}]
           :accessibility-label :view-chat-details-button
           :accessories         [:chevron]
           :on-press            #(hide-sheet-and-dispatch  [:chat.ui/show-profile from])}])
       [list-item/list-item
        {:theme    :action
         :title    :t/message-reply
         :icon     :main-icons/reply
         :on-press #(hide-sheet-and-dispatch [:chat.ui/reply-to-message message])}]
       [list-item/list-item
        {:theme    :action
         :title    :t/sharing-copy-to-clipboard
         :icon     :main-icons/copy
         :on-press (fn []
                     (re-frame/dispatch [:bottom-sheet/hide-sheet])
                     (react/copy-to-clipboard (:text content)))}]
       (when-not platform/desktop?
         [list-item/list-item
          {:theme    :action
           :title    :t/sharing-share
           :icon     :main-icons/share
           :on-press (fn []
                       (re-frame/dispatch [:bottom-sheet/hide-sheet])
                       (list-selection/open-share {:message (:text content)}))}])])))

(defn sticker-long-press [{:keys [from]}]
  (fn []
    (let [photo @(re-frame/subscribe [:chats/photo-path from])
          contact-name @(re-frame/subscribe [:contacts/contact-name-by-identity from])]
      [react/view
       [list-item/list-item
        {:theme               :action
         :icon                photo
         :title               [view-profile {:name   contact-name
                                             :helper :t/view-profile}]
         :accessibility-label :view-chat-details-button
         :accessories         [:chevron]
         :on-press            #(hide-sheet-and-dispatch  [:chat.ui/show-profile from])}]])))

(defn image-long-press [{:keys [content identicon from outgoing] :as message} from-preview?]
  (fn []
    (let [{:keys [ens-name alias]} @(re-frame/subscribe [:contacts/contact-name-by-identity from])]
      [react/view
       (when-not outgoing
         [list-item/list-item
          {:theme               :action
           :icon                (multiaccounts/displayed-photo {:identicon  identicon
                                                                :public-key from})
           :title               [view-profile {:name   (or ens-name alias)
                                               :helper :t/view-profile}]
           :accessibility-label :view-chat-details-button
           :accessories         [:chevron]
           :on-press            #(do
                                   (when from-preview?
                                     (re-frame/dispatch [:navigate-back]))
                                   (hide-sheet-and-dispatch  [:chat.ui/show-profile from]))}])
       [list-item/list-item
        {:theme    :action
         :title    :t/message-reply
         :icon     :main-icons/reply
         :on-press #(do
                      (when from-preview?
                        (re-frame/dispatch [:navigate-back]))
                      (hide-sheet-and-dispatch [:chat.ui/reply-to-message message]))}]
       ;; we have only base64 string for image, so we need to find a way how to copy it
       #_[list-item/list-item
          {:theme    :action
           :title    :t/sharing-copy-to-clipboard
           :icon     :main-icons/copy
           :on-press (fn []
                       (re-frame/dispatch [:bottom-sheet/hide-sheet])
                       (react/copy-to-clipboard (:image content)))}]
       [list-item/list-item
        {:theme    :action
         :title    :t/save
         :icon     :main-icons/download
         :on-press (fn []
                     (hide-sheet-and-dispatch [:chat.ui/save-image-to-gallery (:image content)]))}]
       ;; we have only base64 string for image, so we need to find a way how to share it
       #_[list-item/list-item
          {:theme    :action
           :title    :t/sharing-share
           :icon     :main-icons/share
           :on-press (fn []
                       (re-frame/dispatch [:bottom-sheet/hide-sheet])
                       (list-selection/open-share {:message (:image content)}))}]])))
