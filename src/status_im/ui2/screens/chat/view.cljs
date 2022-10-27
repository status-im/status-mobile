(ns status-im.ui2.screens.chat.view
  (:require [reagent.core :as reagent]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui2.screens.chat.composer.view :as composer]
            [status-im.utils.debounce :as debounce]
            [quo.react-native :as rn]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.navigation.state :as navigation.state]
            [status-im.ui2.screens.chat.messages.view :as messages]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui2.screens.chat.messages.pinned-message :as pinned-message]
            [re-frame.db]
            [status-im.ui2.screens.chat.messages.message :as message]))

(defn topbar-content []
  (let [window-width (<sub [:dimensions/window-width])
        {:keys [group-chat chat-id chat-name]} (<sub [:chats/current-chat])]
    [rn/view {:flex-direction :row :align-items :center :height 56}
     [rn/touchable-highlight {:on-press #(when-not group-chat
                                           (debounce/dispatch-and-chill [:chat.ui/show-profile chat-id] 1000))
                              :style    {:flex 1 :margin-left 12 :width (- window-width 120)}}
      [rn/view
       [rn/text chat-name]]]]))

(defn back-button []
  [quo2.button/button {:type                :grey
                       :size                32
                       :width               32
                       :accessibility-label "back-button"
                       :on-press            #(do
                                               (>evt [:close-chat])
                                               (>evt [:navigate-back]))}
   [icons/icon :main-icons/arrow-left {:color (colors/theme-colors colors/neutral-100 colors/white)}]])

(defn search-button []
  [quo2.button/button {:type                :grey
                       :size                32
                       :width               32
                       :accessibility-label "search-button"}
   [icons/icon :main-icons/search {:color (colors/theme-colors colors/neutral-100 colors/white)}]])

(defn navigate-back-handler []
  (when (and (not @navigation.state/curr-modal) (= (get @re-frame.db/app-db :view-id) :chat))
    (react/hw-back-remove-listener navigate-back-handler)
    (>evt [:close-chat])
    (>evt [:navigate-back])))

(defn chat-render []
  (let [{:keys [chat-id show-input?] :as chat}
        ;;we want to react only on these fields, do not use full chat map here
        (<sub [:chats/current-chat-chat-view])
        mutual-contact-requests-enabled? (<sub [:mutual-contact-requests/enabled?])]
    [react/keyboard-avoiding-view-new {:style         {:flex 1}
                                       :ignore-offset false}
     ;;TODO It is better to not use topbar component because of performance
     [topbar/topbar {:navigation      :none
                     :left-component  [rn/view {:flex-direction :row :margin-left 16}
                                       [back-button]]
                     :title-component [topbar-content]
                     :right-component [rn/view {:flex-direction :row :margin-right 16}
                                       [search-button]]
                     :border-bottom   false
                     :new-ui?         true}]
     [connectivity/loading-indicator]
     ;;TODO not implemented
     #_(when chat-id
         (if group-chat
           [invitation-requests chat-id admins]
           (when-not mutual-contact-requests-enabled? [add-contact-bar chat-id])))
     [pinned-message/pin-limit-popover chat-id message/pinned-messages-list]
     [message/pinned-banner chat-id]
     ;;MESSAGES LIST
     [messages/messages-view
      {:chat                             chat
       :mutual-contact-requests-enabled? mutual-contact-requests-enabled?
       :show-input?                      show-input?}]
     ;;INPUT COMPOSER
     (when show-input?
       [composer/composer chat-id])]))

(defn chat []
  (reagent/create-class
   {:component-did-mount    (fn []
                              (react/hw-back-remove-listener navigate-back-handler)
                              (react/hw-back-add-listener navigate-back-handler))
    :component-will-unmount (fn [] (react/hw-back-remove-listener navigate-back-handler))
    :reagent-render         chat-render}))
