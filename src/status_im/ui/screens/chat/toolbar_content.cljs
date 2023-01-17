(ns status-im.ui.screens.chat.toolbar-content
  (:require [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.screens.chat.styles.main :as st]))

(defn- group-last-activity
  [{:keys [contacts public?]}]
  [rn/view {:flex-direction :row}
   [rn/text {:style st/toolbar-subtitle}
    (if public?
      (i18n/label :t/public-group-status)
      (let [cnt (count contacts)]
        (if (zero? cnt)
          (i18n/label :members-active-none)
          (i18n/label-pluralize cnt :t/members-active))))]])

(defn one-to-one-name
  [from]
  (let [[first-name _] @(re-frame.core/subscribe [:contacts/contact-two-names-by-identity from])]
    [rn/text
     {:style               st/chat-name-text
      :number-of-lines     1
      :accessibility-label :chat-name-text}
     first-name]))

(defn contact-indicator
  [contact-id]
  (let [added? @(re-frame/subscribe [:contacts/contact-added? contact-id])]
    [rn/view {:flex-direction :row}
     [rn/text {:style st/toolbar-subtitle}
      (if added?
        (i18n/label :chat-is-a-contact)
        (i18n/label :chat-is-not-a-contact))]]))

(defn toolbar-content-view-inner
  [chat-info]
  (let [{:keys [group-chat invitation-admin color chat-id contacts chat-type chat-name public? emoji]}
        chat-info]
    [rn/view {:style st/toolbar-container}
     [rn/view {:margin-right 10}
      [chat-icon.screen/chat-icon-view-toolbar chat-id group-chat chat-name color emoji 36]]
     [rn/view {:style st/chat-name-view}
      (if group-chat
        [rn/text
         {:style               st/chat-name-text
          :number-of-lines     1
          :accessibility-label :chat-name-text}
         chat-name]
        [one-to-one-name chat-id])
      (when-not group-chat
        [contact-indicator chat-id])
      (when (and group-chat (not invitation-admin) (not= chat-type constants/community-chat-type))
        [group-last-activity
         {:contacts contacts
          :public?  public?}])]]))
