(ns status-im.ui.screens.chat.views
  (:require [re-frame.core :as re-frame]
            [status-im.contact.core :as models.contact]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.chat.actions :as actions]
            [status-im.ui.screens.chat.bottom-info :as bottom-info]
            [status-im.ui.screens.chat.input.input :as input]
            [status-im.ui.screens.chat.message.datemark :as message-datemark]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.screens.chat.message.options :as message-options]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.ui.screens.chat.toolbar-content :as toolbar-content]
            [status-im.utils.platform :as platform]
            [reagent.core :as reagent])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn add-contact-bar [{:keys [hide-contact? public-key] :as contact}]
  (when (and (not hide-contact?)
             (models.contact/can-add-to-contacts? contact))
    [react/view style/add-contact
     [react/view style/add-contact-left]
     [react/touchable-highlight
      {:on-press            #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])
       :accessibility-label :add-to-contacts-button}
      [react/view style/add-contact-center
       [vector-icons/icon :icons/add {:color colors/blue}]
       [react/i18n-text {:style style/add-contact-text :key :add-to-contacts}]]]
     [react/touchable-highlight
      {:on-press            #(re-frame/dispatch [:contacts.ui/close-contact-pressed public-key])
       :accessibility-label :add-to-contacts-close-button}
      [vector-icons/icon :icons/close {:color           colors/black
                                       :container-style style/add-contact-close-icon}]]]))

(defn- on-options [chat-id chat-name group-chat? public?]
  (list-selection/show {:title   chat-name
                        :options (actions/actions group-chat? chat-id public?)}))

(defn chat-toolbar
  [{:keys [name public? group-chat chat-id contacts contact] :as current-chat} modal?]
  [react/view
   [status-bar/status-bar (when modal? {:type :modal-white})]
   [toolbar/platform-agnostic-toolbar {}
    (if modal?
      [toolbar/nav-button
       (toolbar.actions/close toolbar.actions/default-handler)]
      (toolbar/nav-back-count {:home? true}))
    [toolbar-content/toolbar-content-view current-chat]
    (when-not modal?
      [toolbar/actions [{:icon      :icons/options
                         :icon-opts {:color               :black
                                     :accessibility-label :chat-menu-button}
                         :handler   #(on-options chat-id name group-chat public?)}]])]
   (when-not (or public? group-chat) [add-contact-bar contact])])

(defmulti message-view (fn [{{:keys [type]} :message}] type))

(defmethod message-view :datemark
  [{{:keys [value]} :message}]
  [message-datemark/chat-datemark value])

(defmethod message-view :default
  [{:keys [group-chat modal? message]}]
  [message/chat-message (assoc message
                               :group-chat group-chat
                               :modal? modal?)])

(defn messages-view-animation [messages-view]
  ;; smooths out appearance of messages-view
  (reagent/create-class
   (let [opacity       (animation/create-value 0)
         duration      (if platform/android? 100 200)
         timeout       (if platform/android? 50 0)]
     {:component-did-mount (fn [_]
                             (animation/start
                              (animation/anim-sequence
                               [(animation/anim-delay timeout)
                                (animation/spring opacity {:toValue  1
                                                           :duration duration
                                                           :useNativeDriver true})])))}
     {:reagent-render (fn [messages-view]
                        [react/with-activity-indicator
                         {:style   style/message-view-preview
                          :preview [react/view style/message-view-preview]}
                         [react/touchable-without-feedback
                          {:on-press (fn [_]
                                       (re-frame/dispatch [:chat.ui/set-chat-ui-props {:messages-focused? true}])
                                       (react/dismiss-keyboard!))}
                          [react/animated-view {:style (style/message-view-animated opacity)}
                           messages-view]]])})))

(defn empty-chat-container [{:keys [group-chat chat-id contact]}]
  (let [one-to-one (and (not group-chat)
                        (not (:dapp? contact)))]
    [react/view style/empty-chat-container
     (when one-to-one
       [vector-icons/icon :icons/lock])
     [react/text {:style style/empty-chat-text}
      (if one-to-one
        [react/text style/empty-chat-container-one-to-one
         (i18n/label :t/empty-chat-description-one-to-one)
         [react/text {:style style/empty-chat-text-name} (:name contact)]]
        (i18n/label :t/empty-chat-description))]]))

(defn messages-view
  [messages modal?]
  (reagent/create-class
   {:component-did-mount #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:messages-focused? true
                                                                          :input-focused? false}])
    :reagent-render (fn [{:keys [group-chat contact messages] :as chat} modal?]
                      (if (empty? messages)
                        [empty-chat-container chat]
                        [list/flat-list {:data                      messages
                                         :key-fn                    #(or (:message-id %) (:value %))
                                         :render-fn                 (fn [message]
                                                                      [message-view {:group-chat group-chat
                                                                                     :modal?     modal?
                                                                                     :message    message}])
                                         :inverted                  true
                                         :onEndReached              #(re-frame/dispatch [:chat.ui/load-more-messages])
                                         :enableEmptySections       true
                                         :keyboardShouldPersistTaps :handled}]))}))

(defview chat-root [modal?]
  (letsubs [{:keys [group-chat] :as current-chat} [:chats/current]
            show-bottom-info? [:chat/current-chat-ui-prop :show-bottom-info?]
            show-message-options? [:chat/current-chat-ui-prop :show-message-options?]
            current-view      [:get :view-id]]
    ;; this scroll-view is a hack that allows us to use on-blur and on-focus on Android
    ;; more details here: https://github.com/facebook/react-native/issues/11071
    [react/scroll-view {:scroll-enabled               false
                        :style                        style/scroll-root
                        :content-container-style      style/scroll-root
                        :keyboard-should-persist-taps :handled}
     [react/view {:style     style/chat-view
                  :on-layout (fn [e]
                               (re-frame/dispatch [:set :layout-height (-> e .-nativeEvent .-layout .-height)]))}
      [chat-toolbar current-chat modal?]
      (if (or (= :chat current-view) modal?)
        [messages-view-animation
         [messages-view current-chat modal?]]
        [react/view style/message-view-preview])
      [input/container]
      (when show-bottom-info?
        [bottom-info/bottom-info-view])
      (when show-message-options?
        [message-options/view])
      [connectivity/error-view {:top (get platform/platform-specific :status-bar-default-height)}]]]))

(defn chat []
  [chat-root false])

(defn chat-modal []
  [chat-root true])
