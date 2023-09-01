(ns status-im2.contexts.chat.messages.view
  (:require [status-im2.contexts.chat.composer.view :as composer]
            [status-im2.contexts.chat.messages.contact-requests.bottom-drawer :as
             contact-requests.bottom-drawer]
            [status-im2.contexts.chat.messages.list.view :as messages.list]
            [status-im2.contexts.chat.messages.navigation.view :as messages.navigation]
            [utils.re-frame :as rf]))

(defn chat
  []
  (let [{:keys [chat-id
                contact-request-state
                group-chat
                able-to-send-message?]
         :as   current-chat} (rf/sub [:chats/current-chat-chat-view])]
    [messages.list/messages-list
     {:cover-bg-color :turquoise
      :chat           current-chat
      :header-comp    (fn [{:keys [scroll-y]}]
                        [messages.navigation/navigation-view {:scroll-y scroll-y}])
      :footer-comp    (when (some? able-to-send-message?)
                        (fn [{:keys [insets]}]
                          (if-not able-to-send-message?
                            [contact-requests.bottom-drawer/view chat-id contact-request-state
                             group-chat]
                            [:f> composer/composer insets])))}]))
