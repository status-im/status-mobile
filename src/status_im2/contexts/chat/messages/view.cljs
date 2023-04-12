(ns status-im2.contexts.chat.messages.view
  (:require [quo2.core :as quo]
            [re-frame.db]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.messages.composer.view :as composer]
            [status-im2.contexts.chat.bottom-sheet-composer.view :as bottom-sheet-composer]
            [status-im2.contexts.chat.messages.contact-requests.bottom-drawer :as
             contact-requests.bottom-drawer]
            [status-im2.contexts.chat.messages.list.view :as messages.list]
            [status-im2.contexts.chat.messages.pin.banner.view :as pin.banner]
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

(defn page-nav
  []
  (let [{:keys [group-chat chat-id chat-name emoji
                chat-type]} (rf/sub [:chats/current-chat])
        display-name        (if (= chat-type constants/one-to-one-chat-type)
                              (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
                              (str emoji " " chat-name))
        online?             (rf/sub [:visibility-status-updates/online? chat-id])
        contact             (when-not group-chat
                              (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path          (rf/sub [:chats/photo-path chat-id])
        avatar-image-key    (if (seq (:images contact))
                              :profile-picture
                              :ring-background)]
    [quo/page-nav
     {:align-mid?            true
      :mid-section           (if group-chat
                               {:type      :text-only
                                :main-text display-name}
                               {:type      :user-avatar
                                :avatar    {:full-name       display-name
                                            :online?         online?
                                            :size            :medium
                                            avatar-image-key photo-path}
                                :main-text display-name
                                :on-press  #(debounce/dispatch-and-chill [:chat.ui/show-profile chat-id]
                                                                         1000)})

      :left-section          {:on-press            #(do
                                                      (rf/dispatch [:chat/close])
                                                      (rf/dispatch [:navigate-back]))
                              :icon                :i/arrow-left
                              :accessibility-label :back-button}

      :right-section-buttons [{:on-press            #()
                               :style               {:border-width 1
                                                     :border-color :red}
                               :icon                :i/options
                               :accessibility-label :options-button}]}]))

(defn chat-render
  []
  (let [;;NOTE: we want to react only on these fields, do not use full chat map here
        {:keys [chat-id contact-request-state group-chat able-to-send-message?] :as chat}
        (rf/sub [:chats/current-chat-chat-view])]
    [safe-area/consumer
     (fn [insets]
       [rn/keyboard-avoiding-view
        {:style                  {:position :relative :flex 1}
         :keyboardVerticalOffset (- (max 20 (:bottom insets)))}
        [page-nav]
        [pin.banner/banner chat-id]
        [messages.list/messages-list chat insets]
        (if-not able-to-send-message?
          [contact-requests.bottom-drawer/view chat-id contact-request-state group-chat]
          ;[composer/composer chat-id insets]
          [bottom-sheet-composer/bottom-sheet-composer chat-id insets]
        )])]))

(defn chat
  []
  (reagent/create-class
   {:component-did-mount    (fn []
                              (rn/hw-back-remove-listener navigate-back-handler)
                              (rn/hw-back-add-listener navigate-back-handler))
    :component-will-unmount (fn [] (rn/hw-back-remove-listener navigate-back-handler))
    :reagent-render         chat-render}))
