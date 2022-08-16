(ns status-im.switcher.cards.messaging-card
  (:require [quo.react-native :as rn]
            [quo2.components.text :as text]
            [status-im.constants :as constants]
            [quo2.components.button :as button]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.switcher.cards.styles :as styles]))

;; TODO - Add switcher close animation (fade) while opening screen from cards
;; currently dealy is added to avoid default circular animation
(defn on-press [id toggle-switcher-screen]
  (js/setTimeout toggle-switcher-screen 100)
  (>evt [:chat.ui/navigate-to-chat-nav2 id true]))

;; TODO - add last message for other content types
(defn last-message [{:keys [content content-type]}]
  (cond
    (= constants/content-type-text content-type)
    [text/text (styles/messaging-card-last-message-text-props) (:text content)]))

(defn card [{:keys [id toggle-switcher-screen]}]
  (let [chat (<sub [:chats/chat id])]
    [rn/touchable-without-feedback {:on-press #(on-press id toggle-switcher-screen)}
     [rn/view {:style (styles/messaging-card-main-container)}
      [rn/view {:style (styles/messaging-card-secondary-container)}
       [text/text (styles/messaging-card-title-props) (:alias chat)]
       [text/text (styles/messaging-card-subtitle-props) "Message"]
       [rn/view {:style (styles/messaging-card-details-container)}
        [last-message (:last-message chat)]]]
      [rn/view {:style (styles/messaging-card-avatar-container)}]
      [button/button (styles/messaging-card-close-button-props) :main-icons/close]]]))

