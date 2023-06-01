(ns status-im2.contexts.chat.messages.view
  (:require [quo2.foundations.colors :as colors]
            [re-frame.db]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.chat.composer.view :as composer]
            [status-im2.contexts.chat.messages.contact-requests.bottom-drawer :as
             contact-requests.bottom-drawer]
            [status-im2.contexts.chat.messages.list.view :as messages.list]
            [status-im2.contexts.chat.messages.navigation.view :as messages.navigation]
            [status-im2.navigation.state :as navigation.state]
            [utils.debounce :as debounce]
            [utils.re-frame :as rf]))

(defn navigate-back-handler
  []
  (when (and (not @navigation.state/curr-modal) (= (get @re-frame.db/app-db :view-id) :chat))
    (rn/hw-back-remove-listener navigate-back-handler)
    (rf/dispatch [:chat/close])
    (rf/dispatch [:navigate-back])
    ;; If true is not returned back button event will bubble up,
    ;; and will call system back button action
    true))

(defn chat-render
  []
  (let [{:keys [chat-id
                contact-request-state
                group-chat
                able-to-send-message?]
         :as   chat} (rf/sub [:chats/current-chat-chat-view])]
    [messages.list/messages-list
     {:cover-bg-color (colors/custom-color :turquoise 50 20)
      :chat           chat
      :header-comp    (fn [{:keys [scroll-y]}]
                        [messages.navigation/navigation-view {:scroll-y scroll-y}])
      :footer-comp    (fn [{:keys [insets]}]
                        [rn/view
                         (if-not able-to-send-message?
                           [contact-requests.bottom-drawer/view chat-id contact-request-state group-chat]
                           [:f> composer/composer insets])])}]))

(defn chat
  []
  (reagent/create-class
   {:component-did-mount    (fn []
                              (rn/hw-back-remove-listener navigate-back-handler)
                              (rn/hw-back-add-listener navigate-back-handler))
    :component-will-unmount (fn [] (rn/hw-back-remove-listener navigate-back-handler))
    :reagent-render         chat-render}))
