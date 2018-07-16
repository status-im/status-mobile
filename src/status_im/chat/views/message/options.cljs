(ns status-im.chat.views.message.options
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as re-frame]
            [status-im.thread :as status-im.thread]
            [reagent.core :as reagent]
            [clojure.string :as string]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.chat.views.bottom-info :as bottom-info]
            [status-im.chat.styles.screen :as styles]
            [status-im.chat.styles.message.options :as options.styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.components.list.views :as list]
            [status-im.utils.core :as utils]
            [status-im.utils.identicon :as identicon]
            [status-im.ui.components.icons.vector-icons :as vector-icons]))

(defn action-item [{:keys [label icon style on-press]}]
  [react/touchable-highlight {:on-press on-press}
   [react/view options.styles/row
    [react/view
     [vector-icons/icon icon style]]
    [react/view (merge options.styles/label style)
     [react/i18n-text {:style   (merge options.styles/label-text style)
                       :key     label}]]]])

(defn view []
  (let [{:keys [chat-id message-id]} @(re-frame/subscribe [:get-current-chat-ui-prop :message-options])
        close-message-options-fn #(status-im.thread/dispatch [:set-chat-ui-props {:show-message-options? false}])]
    [bottom-info/overlay {:on-click-outside close-message-options-fn}
     [bottom-info/container (* styles/item-height 2)
      [react/view
       [react/view options.styles/title
        [react/i18n-text {:style options.styles/title-text :key :message-not-sent}]]
       [action-item {:label    :resend-message
                     :icon     :icons/refresh
                     :on-press #(do
                                  (close-message-options-fn)
                                  (status-im.thread/dispatch [:resend-message chat-id message-id]))}]
       [action-item {:label    :delete-message
                     :icon     :icons/delete
                     :style    {:color colors/red}
                     :on-press #(do
                                  (close-message-options-fn)
                                  (status-im.thread/dispatch [:delete-message chat-id message-id]))}]]]]))
