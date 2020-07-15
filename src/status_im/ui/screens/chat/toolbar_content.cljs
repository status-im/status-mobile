(ns status-im.ui.screens.chat.toolbar-content
  (:require [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.main :as st]
            [re-frame.core :as re-frame])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn- group-last-activity [{:keys [contacts public?]}]
  [react/view {:flex-direction :row}
   [react/text {:style st/toolbar-subtitle}
    (if public?
      (i18n/label :t/public-group-status)
      (let [cnt (count contacts)]
        (if (zero? cnt)
          (i18n/label :members-active-none)
          (i18n/label-pluralize cnt :t/members-active))))]])

(defn one-to-one-name [from]
  @(re-frame.core/subscribe [:contacts/contact-name-by-identity from]))

(defn contact-indicator [contact-id]
  (let [added? @(re-frame/subscribe [:contacts/contact-added? contact-id])]
    [react/view {:flex-direction :row}
     [react/text {:style st/toolbar-subtitle}
      (if added?
        (i18n/label :chat-is-a-contact)
        (i18n/label :chat-is-not-a-contact))]]))

(defview toolbar-content-view []
  (letsubs [{:keys [group-chat
                    color
                    chat-id
                    contacts
                    chat-name
                    public?]}
            [:chats/current-chat]]
    [react/view {:style st/toolbar-container}
     [react/view {:margin-right 10}
      [chat-icon.screen/chat-icon-view-toolbar chat-id group-chat chat-name color]]
     [react/view {:style st/chat-name-view}
      [react/text {:style               st/chat-name-text
                   :number-of-lines     1
                   :accessibility-label :chat-name-text}
       (if group-chat
         chat-name
         [one-to-one-name chat-id])]
      (when-not group-chat
        [contact-indicator chat-id])
      (when group-chat
        [group-last-activity {:contacts   contacts
                              :public?    public?}])]]))
