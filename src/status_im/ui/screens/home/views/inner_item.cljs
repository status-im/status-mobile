(ns status-im.ui.screens.home.views.inner-item
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.components.styles :as component.styles]
            [status-im.utils.core :as utils]
            [status-im.commands.utils :as commands-utils]
            [status-im.i18n :as i18n]
            [status-im.utils.datetime :as time]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.constants :as const]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.common.common :as components.common]))

(defn message-content-text [{:keys [content] :as message}]
  [react/view styles/last-message-container
   (cond

     (not message)
     [react/text {:style               styles/last-message-text
                  :accessibility-label :no-messages-text}
      (i18n/label :t/no-messages)]

     (str/blank? content)
     [react/text {:style styles/last-message-text}
      ""]

     (:content content)
     [react/text {:style               styles/last-message-text
                  :number-of-lines     1
                  :accessibility-label :chat-message-text}
      (:content content)]

     (and (:command content) (-> content :short-preview :markup))
     (commands-utils/generate-hiccup (-> content :short-preview :markup))

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
  (letsubs [unviewed-messages-count [:unviewed-messages-count chat-id]]
    (when (pos? unviewed-messages-count)
      [components.common/counter {:size                22
                                  :accessibility-label :unread-messages-count-text}
       unviewed-messages-count])))

(defn chat-list-item-name [name group-chat? public? public-key]
  (let [private-group? (and group-chat? (not public?))
        public-group?  (and group-chat? public?)
        chat-name      (if (str/blank? name)
                         (gfycat/generate-gfy public-key)
                         (utils/truncate-str name 30))]
    [react/view styles/name-view
     (when public-group?
       [react/view styles/public-group-icon-container
        [vector-icons/icon :icons/public-chat {:color colors/gray}]])
     (when private-group?
       [react/view styles/private-group-icon-container
        [vector-icons/icon :icons/group-chat {:color colors/gray}]])
     [react/view {:flex-shrink 1}
      [react/text {:style               styles/name-text
                   :number-of-lines     1
                   :accessibility-label :chat-name-text}
       (if public-group?
         (str "#" chat-name)
         chat-name)]]]))

(defview home-list-chat-item-inner-view [{:keys [chat-id name color online
                                                 group-chat public?
                                                 public-key
                                                 timestamp]}]
  (letsubs [last-message [:get-last-message chat-id]]
    (let [name (or (i18n/get-contact-translated chat-id :name name)
                   (gfycat/generate-gfy public-key))
          hide-dapp? (= chat-id const/console-chat-id)]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to-chat chat-id])}
       [react/view styles/chat-container
        [react/view styles/chat-icon-container
         [chat-icon.screen/chat-icon-view-chat-list chat-id group-chat name color online hide-dapp?]]
        [react/view styles/chat-info-container
         [react/view styles/item-upper-container
          [chat-list-item-name name group-chat public? public-key]
          [react/view styles/message-status-container
           [message-timestamp timestamp]]]
         [react/view styles/item-lower-container
          [message-content-text last-message]
          [unviewed-indicator chat-id]]]]])))

(defview home-list-browser-item-inner-view [{:keys [name url] :as browser}]
  (letsubs [dapp [:get-dapp-by-name name]]
    [react/touchable-highlight {:on-press #(re-frame/dispatch [:open-browser browser])}
     [react/view styles/chat-container
      [react/view styles/chat-icon-container
       (if dapp
         [chat-icon.screen/dapp-icon-browser dapp 36]
         [react/view styles/browser-icon-container
          [vector-icons/icon :icons/discover {:color component.styles/color-light-gray6}]])]
      [react/view styles/chat-info-container
       [react/view styles/item-upper-container
        [react/view styles/name-view
         [react/view {:flex-shrink 1}
          [react/text {:style               styles/name-text
                       :accessibility-label :chat-name-text
                       :number-of-lines     1}
           name]]]]
       [react/view styles/item-lower-container
        [react/view styles/last-message-container
         [react/text {:style               styles/last-message-text
                      :accessibility-label :chat-url-text
                      :number-of-lines     1}
          (or url (i18n/label :t/dapp))]]]]]]))
