(ns status-im.ui.screens.chat.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.contact.core :as models.contact]
            [status-im.chat.models :as models.chat]
            [status-im.group-chats.core :as models.group-chats]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.chat.input.input :as input]
            [status-im.ui.screens.chat.actions :as actions]
            [status-im.ui.screens.chat.bottom-info :as bottom-info]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.screens.chat.message.options :as message-options]
            [status-im.ui.screens.chat.message.datemark :as message-datemark]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.screens.chat.toolbar-content :as toolbar-content]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.button.view :as buttons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.screens.chat.stickers.views :as stickers]))

(defview add-contact-bar [contact-identity]
  (letsubs [{:keys [hide-contact?] :as contact} [:contacts/contact-by-identity]]
    (when (and (not hide-contact?)
               (models.contact/can-add-to-contacts? contact))
      [react/view style/add-contact
       [react/view style/add-contact-left]
       [react/touchable-highlight
        {:on-press            #(re-frame/dispatch [:contact.ui/add-to-contact-pressed contact-identity])
         :accessibility-label :add-to-contacts-button}
        [react/view style/add-contact-center
         [vector-icons/icon :main-icons/add {:color colors/blue}]
         [react/i18n-text {:style style/add-contact-text :key :add-to-contacts}]]]
       [react/touchable-highlight
        {:on-press            #(re-frame/dispatch [:contact.ui/close-contact-pressed contact-identity])
         :accessibility-label :add-to-contacts-close-button}
        [vector-icons/icon :main-icons/close {:color           colors/black
                                              :container-style style/add-contact-close-icon}]]])))

(defn- on-options [chat-id chat-name group-chat? public?]
  (list-selection/show {:title   chat-name
                        :options (actions/actions group-chat? chat-id public?)}))

(defview chat-toolbar [public? modal?]
  (letsubs [name                                  [:chats/current-chat-name]
            {:keys [group-chat chat-id contacts]} [:chats/current-chat]]
    [react/view
     [status-bar/status-bar (when modal? {:type :modal-white})]
     [toolbar/platform-agnostic-toolbar {}
      (if modal?
        [toolbar/nav-button
         (toolbar.actions/close toolbar.actions/default-handler)]
        (toolbar/nav-back-count {:home? true}))
      [toolbar-content/toolbar-content-view]
      (when-not modal?
        [toolbar/actions [{:icon      :main-icons/more
                           :icon-opts {:color               :black
                                       :accessibility-label :chat-menu-button}
                           :handler   #(on-options chat-id name group-chat public?)}]])]
     [connectivity/connectivity-view]
     (when-not (or public? group-chat)
       [add-contact-bar (first contacts)])]))

(defmulti message-row (fn [{{:keys [type]} :row}] type))

(defmethod message-row :datemark
  [{{:keys [value]} :row}]
  [message-datemark/chat-datemark value])

(defmethod message-row :default
  [{:keys [group-chat current-public-key modal? row]}]
  [message/chat-message (assoc row
                               :group-chat group-chat
                               :modal? modal?
                               :current-public-key current-public-key)])

(defview messages-view-animation [message-view]
  ;; smooths out appearance of message-view
  (letsubs [opacity       (animation/create-value 0)
            duration      (if platform/android? 100 200)
            timeout       (if platform/android? 50 0)]
    {:component-did-mount (fn [_]
                            (animation/start
                             (animation/anim-sequence
                              [(animation/anim-delay timeout)
                               (animation/spring opacity {:toValue  1
                                                          :duration duration
                                                          :useNativeDriver true})])))}
    [react/with-activity-indicator
     {:style   style/message-view-preview
      :preview [react/view style/message-view-preview]}
     [react/touchable-without-feedback
      {:on-press (fn [_]
                   (re-frame/dispatch [:chat.ui/set-chat-ui-props {:messages-focused? true}])
                   (react/dismiss-keyboard!))}
      [react/animated-view {:style (style/message-view-animated opacity)}
       message-view]]]))

(defview empty-chat-container [{:keys [group-chat chat-id]}]
  (letsubs [contact [:contacts/contact-by-identity chat-id]]
    (let [one-to-one (and (not group-chat)
                          (not (:dapp? contact)))]
      [react/view style/empty-chat-container
       (when one-to-one
         [vector-icons/icon :tiny-icons/tiny-lock])
       [react/text {:style style/empty-chat-text}
        (if one-to-one
          [react/text style/empty-chat-container-one-to-one
           (i18n/label :t/empty-chat-description-one-to-one)
           [react/text {:style style/empty-chat-text-name} (:name contact)]]
          (i18n/label :t/empty-chat-description))]])))

(defn join-chat-button [chat-id]
  [buttons/secondary-button {:style style/join-button
                             :on-press #(re-frame/dispatch [:group-chats.ui/join-pressed chat-id])}
   (i18n/label :t/join-group-chat)])

(defn decline-chat [chat-id]
  [react/touchable-highlight
   {:on-press
    #(re-frame/dispatch [:group-chats.ui/remove-chat-confirmed chat-id])}
   [react/text {:style style/decline-chat}
    (i18n/label :t/group-chat-decline-invitation)]])

(defview group-chat-join-section [my-public-key {:keys [name
                                                        group-chat
                                                        color
                                                        chat-id] :as chat}]
  (letsubs [contact [:contacts/contact-by-identity (models.group-chats/get-inviter-pk my-public-key chat)]]
    [react/view style/empty-chat-container
     [react/view {:style {:margin-bottom 170}}
      [chat-icon.screen/profile-icon-view nil name color false 100 {:default-chat-icon-text style/group-chat-icon}]]
     [react/view {:style style/group-chat-join-footer}
      [react/view {:style style/group-chat-join-container}
       [react/view
        [react/text {:style style/group-chat-join-name} name]]
       [react/text {:style style/empty-chat-text}
        [react/text style/empty-chat-container-one-to-one
         (i18n/label :t/join-group-chat-description {:username (:name contact)
                                                     :group-name name})]]
       [join-chat-button chat-id]
       [decline-chat chat-id]]]]))

(defview messages-view [{:keys [group-chat] :as chat} modal?]
  (letsubs [messages           [:chats/current-chat-messages-stream]
            current-public-key [:account/public-key]]
    {:component-did-mount
     (fn [args]
       (when-not (:messages-initialized? (second (.-argv (.-props args))))
         (re-frame/dispatch [:chat.ui/load-more-messages]))
       (re-frame/dispatch [:chat.ui/set-chat-ui-props
                           {:messages-focused? true
                            :input-focused?    false}]))}
    (cond

      (and (models.chat/group-chat? chat)
           (models.group-chats/invited? current-public-key chat)
           (not (models.group-chats/joined? current-public-key chat)))
      [group-chat-join-section current-public-key chat]

      (and (empty? messages)
           (:messages-initialized? chat))
      [empty-chat-container chat]

      :else
      [list/flat-list {:data                      messages
                       :key-fn                    #(or (:message-id %) (:value %))
                       :render-fn                 (fn [message]
                                                    [message-row {:group-chat         group-chat
                                                                  :modal?             modal?
                                                                  :current-public-key current-public-key
                                                                  :row                message}])
                       :inverted                  true
                       :onEndReached              #(re-frame/dispatch [:chat.ui/load-more-messages])
                       :enableEmptySections       true
                       :keyboardShouldPersistTaps :handled}])))

(defview messages-view-wrapper [modal?]
  (letsubs [chat               [:chats/current-chat]]
    [messages-view chat modal?]))

(defn show-input-container? [my-public-key current-chat]
  (or (not (models.chat/group-chat? current-chat))
      (models.group-chats/joined? my-public-key current-chat)))

(defview chat-root [modal?]
  (letsubs [{:keys [public?] :as current-chat} [:chats/current-chat]
            my-public-key                      [:account/public-key]
            show-bottom-info?                  [:chats/current-chat-ui-prop :show-bottom-info?]
            show-message-options?              [:chats/current-chat-ui-prop :show-message-options?]
            show-stickers?                     [:chats/current-chat-ui-prop :show-stickers?]
            current-view                       [:get :view-id]]
    ;; this scroll-view is a hack that allows us to use on-blur and on-focus on Android
    ;; more details here: https://github.com/facebook/react-native/issues/11071
    [react/scroll-view {:scroll-enabled               false
                        :style                        style/scroll-root
                        :content-container-style      style/scroll-root
                        :keyboard-should-persist-taps :handled}
     [react/view {:style     style/chat-view
                  :on-layout (fn [e]
                               (re-frame/dispatch [:set :layout-height (-> e .-nativeEvent .-layout .-height)]))}
      [chat-toolbar public? modal?]
      (if (or (= :chat current-view) modal?)
        [messages-view-animation
         [messages-view-wrapper modal?]]
        [react/view style/message-view-preview])
      (when (show-input-container? my-public-key current-chat)
        [input/container])
      (when show-stickers?
        [stickers/stickers-view])
      (when show-bottom-info?
        [bottom-info/bottom-info-view])
      (when show-message-options?
        [message-options/view])]]))

(defview chat []
  [chat-root false])

(defview chat-modal []
  [chat-root true])
