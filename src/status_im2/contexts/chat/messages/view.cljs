(ns status-im2.contexts.chat.messages.view
  (:require [quo2.core :as quo]
            [re-frame.db]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im.ui2.screens.chat.composer.view :as composer]
            [status-im.ui2.screens.chat.pin-limit-popover.view :as pin-limit-popover]
            [status-im2.common.constants :as constants]
            [status-im2.contexts.chat.messages.list.view :as messages.list]
            [status-im2.contexts.chat.messages.pin.banner.view :as pin.banner] ;;TODO move to status-im2
            [status-im2.navigation.state :as navigation.state]
            [utils.debounce :as debounce]
            [utils.re-frame :as rf]))

(defn navigate-back-handler
  []
  (when (and (not @navigation.state/curr-modal) (= (get @re-frame.db/app-db :view-id) :chat))
    (rn/hw-back-remove-listener navigate-back-handler)
    (rf/dispatch [:close-chat])
    (rf/dispatch [:navigate-back])))

(defn page-nav
  []
  (let [{:keys [group-chat chat-id chat-name emoji chat-type]} (rf/sub [:chats/current-chat])
        display-name (if (= chat-type constants/one-to-one-chat-type)
                       (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
                       (str emoji " " chat-name))
        online? (rf/sub [:visibility-status-updates/online? chat-id])
        contact (when-not group-chat (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path (when-not (empty? (:images contact)) (rf/sub [:chats/photo-path chat-id]))]
    [quo/page-nav
     {:align-mid?            true

      :mid-section
      (if group-chat
        {:type      :text-only
         :main-text display-name}
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
      [{:on-press            #()                            ;; TODO not implemented
        :icon                :i/options
        :accessibility-label :options-button}]}]))

(defn chat-render
  []
  (let [;;we want to react only on these fields, do not use full chat map here
        {:keys [chat-id show-input?] :as chat} (rf/sub [:chats/current-chat-chat-view])
        mutual-contact-requests-enabled?       (rf/sub [:mutual-contact-requests/enabled?])]
    [rn/keyboard-avoiding-view {:style {:flex 1}}
     [page-nav]
     ;; TODO (flexsurfer) this should be in-app notification component in quo2
     ;; https://github.com/status-im/status-mobile/issues/14527
     [pin-limit-popover/pin-limit-popover chat-id]
     [pin.banner/banner chat-id]
     ;;MESSAGES LIST
     [messages.list/messages-list
      {:chat                             chat
       :mutual-contact-requests-enabled? mutual-contact-requests-enabled?
       :show-input?                      show-input?
       :bottom-space                     15}]
     ;;INPUT COMPOSER
     (when show-input?
       [composer/composer chat-id])]))

(defn chat
  []
  (reagent/create-class
   {:component-did-mount    (fn []
                              (rn/hw-back-remove-listener navigate-back-handler)
                              (rn/hw-back-add-listener navigate-back-handler))
    :component-will-unmount (fn [] (rn/hw-back-remove-listener navigate-back-handler))
    :reagent-render         chat-render}))
