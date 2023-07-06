(ns status-im2.contexts.chat.messages.view
  (:require [status-im2.contexts.chat.composer.view :as composer]
            [status-im2.contexts.chat.messages.contact-requests.bottom-drawer :as
             contact-requests.bottom-drawer]
            [status-im2.contexts.chat.messages.list.view :as messages.list]
            [status-im2.contexts.chat.messages.navigation.view :as messages.navigation]
            [utils.re-frame :as rf]))

(defn load-composer
  [insets chat-type]
  (let [shell-animation-complete? (rf/sub [:shell/animation-complete? chat-type])]
    (when shell-animation-complete?
      [:f> composer/composer insets])))

(defn chat
  []
  (let [{:keys [chat-id
                chat-type
                contact-request-state
                group-chat
                able-to-send-message?]
         :as   chat} (rf/sub [:chats/current-chat-chat-view])]
    [messages.list/messages-list
     {:cover-bg-color :turquoise
      :chat           chat
      :header-comp    (fn [{:keys [scroll-y]}]
                        [:f>
                         messages.navigation/f-navigation-view
                         {:scroll-y scroll-y}])
      :footer-comp    (fn [{:keys [insets]}]
                        (if-not able-to-send-message?
                          [contact-requests.bottom-drawer/view chat-id contact-request-state
                           group-chat]
                          [load-composer insets chat-type]))}]))
