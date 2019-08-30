(ns status-im.ui.screens.home.views.inner-item
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.receiving :as commands-receiving]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.core :as utils]
            [status-im.utils.datetime :as time])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview command-short-preview [message]
  (letsubs [id->command [:chats/id->command]
            {:keys [contacts]} [:chats/current-chat]]
    (when-let [command (commands-receiving/lookup-command-by-ref message id->command)]
      (commands/generate-short-preview command (commands/add-chat-contacts contacts message)))))

(defn message-content-text [{:keys [content content-type] :as message}]
  [react/view styles/last-message-container
   (cond

     (not (and content content-type))
     [react/text {:style               styles/last-message-text
                  :accessibility-label :no-messages-text}
      (i18n/label :t/no-messages)]

     (= constants/content-type-command content-type)
     [command-short-preview message]

     (= constants/content-type-sticker content-type)
     [react/image {:style {:margin 2 :width 30 :height 30}
                   :source {:uri (contenthash/url (:hash content))}}]

     (string/blank? (:text content))
     [react/text {:style styles/last-message-text}
      ""]

     (:text content)
     [react/text {:style               styles/last-message-text
                  :number-of-lines     1
                  :accessibility-label :chat-message-text}
      (:text content)]

     :else
     [react/text {:style               styles/last-message-text
                  :number-of-lines     1
                  :accessibility-label :chat-message-text}
      content])])

(defn message-timestamp [timestamp]
  (when timestamp
    [react/text {:style               styles/datetime-text
                 :accessibility-label :last-message-time-text}
     (time/to-short-str timestamp)]))

(defview unviewed-indicator [chat-id]
  (letsubs [unviewed-messages-count [:chats/unviewed-messages-count chat-id]]
    (when (pos? unviewed-messages-count)
      [components.common/counter {:size                22
                                  :accessibility-label :unread-messages-count-text}
       unviewed-messages-count])))

(defn chat-list-item-name [chat-name group-chat? public? public-key]
  (let [private-group? (and group-chat? (not public?))
        public-group?  (and group-chat? public?)]
    [react/view styles/name-view
     (when public-group?
       [react/view styles/public-group-icon-container
        [vector-icons/tiny-icon :tiny-icons/tiny-public {:color colors/gray}]])
     (when private-group?
       [react/view styles/private-group-icon-container
        [vector-icons/tiny-icon :tiny-icons/tiny-group {:color colors/gray}]])
     [react/view {:flex-shrink 1
                  :align-items :center
                  :justify-content :center}
      [react/text {:style               styles/name-text
                   :number-of-lines     1
                   :accessibility-label :chat-name-text}
       chat-name]]]))

(defn home-list-item [[home-item-id home-item]]
  (let [{:keys [chat-id chat-name
                name color online
                group-chat public?
                public-key contact
                last-message-timestamp
                timestamp
                last-message-content
                last-message-content-type]} home-item
        truncated-chat-name                 (utils/truncate-str chat-name 30)
        chat-actions                        (cond
                                              (and group-chat public?)       :public-chat-actions
                                              (and group-chat (not public?)) :group-chat-actions
                                              :else                          :private-chat-actions)]
    [react/touchable-highlight {:on-press #(do
                                             (re-frame/dispatch [:chat.ui/navigate-to-chat chat-id])
                                             (re-frame/dispatch [:chat.ui/mark-messages-seen :chat]))
                                :on-long-press #(re-frame/dispatch [:bottom-sheet/show-sheet chat-actions {:chat-id chat-id}])}
     [react/view styles/chat-container
      [react/view styles/chat-icon-container
       [chat-icon.screen/chat-icon-view-chat-list contact group-chat truncated-chat-name color online false]]
      [react/view styles/chat-info-container
       [react/view styles/item-upper-container
        [chat-list-item-name truncated-chat-name group-chat public? public-key]
        [react/view styles/message-status-container
         [message-timestamp (if (pos? last-message-timestamp) last-message-timestamp timestamp)]]]
       [react/view styles/item-lower-container
        (let [{:keys [tribute-status tribute-label]} (:tribute-to-talk contact)]
          (if (not (#{:require :pending} tribute-status))
            [message-content-text {:content      last-message-content
                                   :content-type last-message-content-type}]
            [react/text {:style styles/last-message-text} tribute-label]))
        [unviewed-indicator chat-id]]]]]))
