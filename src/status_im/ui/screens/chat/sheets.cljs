(ns status-im.ui.screens.chat.sheets
  (:require
    [re-frame.core :as re-frame]
    [status-im.ui.components.chat-icon.screen :as chat-icon]
    [status-im.ui.components.list.item :as list.item]
    [status-im.ui.components.react :as react]
    [status-im2.constants :as constants]
    [utils.i18n :as i18n]))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (re-frame/dispatch event))

(defn one-to-one-chat-accents
  [chat-id]
  (let [{:keys [primary-name] :as contact} @(re-frame/subscribe [:contacts/contact-by-identity chat-id])]
    [react/view
     [list.item/list-item
      {:theme               :accent
       :icon                [chat-icon/contact-icon-contacts-tab contact]
       :title               primary-name
       :subtitle            (i18n/label :t/view-profile)
       :accessibility-label :view-chat-details-button
       :chevron             true
       :on-press            #(do
                               (hide-sheet-and-dispatch [:chat.ui/show-profile chat-id])
                               (re-frame/dispatch [:pin-message/load-pin-messages chat-id]))}]
     [list.item/list-item
      {:theme               :accent
       :title               (i18n/label :t/mark-all-read)
       :accessibility-label :mark-all-read-button
       :icon                :main-icons/check
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-pressed chat-id])}]
     [list.item/list-item
      {:theme               :negative
       :title               (i18n/label :t/delete-chat)
       :accessibility-label :delete-chat-button
       :icon                :main-icons/delete
       :on-press            #(re-frame/dispatch [:chat.ui/show-remove-confirmation chat-id])}]]))

(defn community-chat-accents
  []
  (fn [{:keys [chat-id group-chat chat-name color emoji]}]
    [react/view
     [list.item/list-item
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
     [list.item/list-item
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
        [list.item/list-item
         {:theme               :accent
          :title               (i18n/label :t/remove)
          :accessibility-label :remove-group-chat
          :icon                :main-icons/delete
          :on-press            #(hide-sheet-and-dispatch [:group-chats.ui/remove-chat-confirmed
                                                          chat-id])}]
        [react/view
         [list.item/list-item
          {:theme    :accent
           :title    chat-name
           :subtitle (i18n/label :t/group-info)
           :icon     [chat-icon/chat-icon-view-chat-sheet
                      chat-id group-chat chat-name color]
           :chevron  true
           :on-press #(do
                        (hide-sheet-and-dispatch [:show-group-chat-profile chat-id])
                        (re-frame/dispatch [:pin-message/load-pin-messages chat-id]))}]
         [list.item/list-item
          {:theme               :accent
           :title               (i18n/label :t/mark-all-read)
           :accessibility-label :mark-all-read-button
           :icon                :main-icons/check
           :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-pressed chat-id])}]
         (when member?
           [list.item/list-item
            {:theme               :negative
             :title               (i18n/label :t/leave-chat)
             :accessibility-label :leave-chat-button
             :icon                :main-icons/arrow-left
             :on-press            #(re-frame/dispatch [:group-chats.ui/leave-chat-pressed chat-id])}])
         (when removed?
           [list.item/list-item
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

    (= chat-type constants/community-chat-type)
    [community-chat-accents current-chat]

    (= chat-type constants/private-group-chat-type)
    [group-chat-accents current-chat]

    :else [one-to-one-chat-accents chat-id]))



