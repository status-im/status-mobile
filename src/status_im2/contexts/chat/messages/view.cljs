(ns status-im2.contexts.chat.messages.view
  (:require [reagent.core :as reagent]
            [re-frame.db]
            [i18n.i18n :as i18n]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [utils.debounce :as debounce]
            [quo2.core :as quo]

            [status-im2.navigation.state :as navigation.state]

            ;;TODO move to status-im2
            [status-im.ui2.screens.chat.composer.view :as composer]
            [status-im.ui2.screens.chat.messages.view :as messages]
            [status-im.ui2.screens.chat.messages.pinned-message :as pinned-message]
            [status-im.ui2.screens.chat.messages.message :as message]))

(defn navigate-back-handler []
  (when (and (not @navigation.state/curr-modal) (= (get @re-frame.db/app-db :view-id) :chat))
    (rn/hw-back-remove-listener navigate-back-handler)
    (rf/dispatch [:close-chat])
    (rf/dispatch [:navigate-back])))

(defn page-nav []
  (let [{:keys [group-chat chat-id chat-name emoji]} (rf/sub [:chats/current-chat])
        display-name (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
        online? (rf/sub [:visibility-status-updates/online? chat-id])
        contact (when-not group-chat (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path (when-not (empty? (:images contact)) (rf/sub [:chats/photo-path chat-id]))]
    [quo/page-nav
     {:align-mid? true

      :mid-section
      (if group-chat
        {:type      :text-only
         :main-text (str emoji " " chat-name)}
        {:type      :user-avatar
         :avatar    {:full-name       display-name
                     :online?         online?
                     :profile-picture photo-path
                     :size            :medium}
         :main-text display-name
         :on-press  #(debounce/dispatch-and-chill [:chat.ui/show-profile chat-id] 1000)})

      :left-section
      {:on-press            #(do
                               (rf/dispatch [:close-chat])
                               (rf/dispatch [:navigate-back]))
       :icon                :i/arrow-left
       :accessibility-label :back-button}

      :right-section-buttons
      [{:on-press            #() ;; TODO not implemented
        :icon                :i/options
        :accessibility-label :options-button}]}]))

(defn chat-render []
  (let [;;we want to react only on these fields, do not use full chat map here
        {:keys [chat-id show-input?] :as chat} (rf/sub [:chats/current-chat-chat-view])
        mutual-contact-requests-enabled? (rf/sub [:mutual-contact-requests/enabled?])]
    [rn/keyboard-avoiding-view {:style {:flex 1}}
     [page-nav]
     [pinned-message/pin-limit-popover chat-id message/pinned-messages-list]
     [message/pinned-banner chat-id]
     ;;MESSAGES LIST
     [messages/messages-view
      {:chat                             chat
       :mutual-contact-requests-enabled? mutual-contact-requests-enabled?
       :show-input?                      show-input?
       :bottom-space                     15}]
     ;;INPUT COMPOSER
     (when show-input?
       [composer/composer chat-id])
     [quo/floating-shell-button
      {:jump-to {:on-press #(rf/dispatch [:shell/navigate-to-jump-to])
                 :label    (i18n/label :t/jump-to)}}
      {:position :absolute
       :bottom   117}]]))

(defn chat []
  (reagent/create-class
   {:component-did-mount    (fn []
                              (rn/hw-back-remove-listener navigate-back-handler)
                              (rn/hw-back-add-listener navigate-back-handler))
    :component-will-unmount (fn [] (rn/hw-back-remove-listener navigate-back-handler))
    :reagent-render         chat-render}))
