(ns status-im.chat.screen
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.chat.models :as models.chat]
            [status-im.models.contact :as models.contact]
            [status-im.chat.styles.screen :as style]
            [status-im.utils.platform :as platform]
            [status-im.chat.views.toolbar-content :as toolbar-content]
            [status-im.chat.views.message.message :as message]
            [status-im.chat.views.message.datemark :as message-datemark]
            [status-im.chat.views.input.input :as input]
            [status-im.chat.views.actions :as actions]
            [status-im.chat.views.bottom-info :as bottom-info]
            [status-im.chat.views.message.options :as message-options]
            [status-im.chat.views.message.datemark :as message-datemark]
            [status-im.chat.views.message.message :as message]
            [status-im.chat.views.toolbar-content :as toolbar-content]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]))

(defview add-contact-bar [contact-identity]
  (letsubs [contact [:get-contact-by-identity contact-identity]]
    (when (models.contact/can-add-to-contacts? contact)
      [react/touchable-highlight
       {:on-press            #(re-frame/dispatch [:add-contact contact-identity])
        :accessibility-label :add-to-contacts-button}
       [react/view style/add-contact
        [react/text {:style style/add-contact-text}
         (i18n/label :t/add-to-contacts)]]])))

(defn- on-options [chat-id chat-name group-chat? public?]
  (list-selection/show {:title   (if public? (str "#" chat-name) chat-name)
                        :options (actions/actions group-chat? chat-id public?)}))

(defview chat-toolbar [public?]
  (letsubs [name                                  [:get-current-chat-name]
            {:keys [group-chat chat-id contacts]} [:get-current-chat]]
    [react/view
     [status-bar/status-bar]
     (if (= chat-id constants/console-chat-id)
       [toolbar/simple-toolbar name]
       [toolbar/platform-agnostic-toolbar {}
        (toolbar/nav-back-count {:home? true})
        [toolbar-content/toolbar-content-view]
        [toolbar/actions [{:icon      :icons/options
                           :icon-opts {:color               :black
                                       :accessibility-label :chat-menu-button}
                           :handler   #(on-options chat-id name group-chat public?)}]]])
     (when-not (or public? group-chat) [add-contact-bar (first contacts)])]))

(defmulti message-row (fn [{{:keys [type]} :row}] type))

(defmethod message-row :datemark
  [{{:keys [value]} :row}]
  [message-datemark/chat-datemark value])

(defmethod message-row :default
  [{:keys [group-chat current-public-key row]}]
  [message/chat-message (assoc row
                               :group-chat group-chat
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
                   (re-frame/dispatch [:set-chat-ui-props {:messages-focused? true}])
                   (react/dismiss-keyboard!))}
      [react/animated-view {:style (style/message-view-animated opacity)}
       message-view]]]))

(defview empty-chat-container [{:keys [group-chat chat-id]}]
  (letsubs [contact [:get-contact-by-identity chat-id]]
    (let [one-to-one (and (not group-chat)
                          (not (:dapp? contact)))]
      [react/view style/empty-chat-container
       (when one-to-one
         [vector-icons/icon :icons/lock])
       [react/text {:style style/empty-chat-text}
        (cond
          (= chat-id constants/console-chat-id)
          (i18n/label :t/empty-chat-description-console)

          one-to-one
          [react/text style/empty-chat-container-one-to-one
           (i18n/label :t/empty-chat-description-one-to-one)
           [react/text {:style style/empty-chat-text-name} (:name contact)]]

          :else
          (i18n/label :t/empty-chat-description))]])))

(defview messages-view [group-chat]
  (letsubs [messages           [:get-current-chat-messages-stream]
            chat               [:get-current-chat]
            current-public-key [:get-current-public-key]]
    {:component-did-mount #(re-frame/dispatch [:set-chat-ui-props {:messages-focused? true
                                                                   :input-focused? false}])}
    (if (empty? messages)
      [empty-chat-container chat]
      [list/flat-list {:data                      messages
                       :key-fn                    #(or (:message-id %) (:value %))
                       :render-fn                 (fn [message]
                                                    [message-row {:group-chat         group-chat
                                                                  :current-public-key current-public-key
                                                                  :row                message}])
                       :inverted                  true
                       :onEndReached              #(re-frame/dispatch [:load-more-messages])
                       :enableEmptySections       true
                       :keyboardShouldPersistTaps :handled}])))

(defview chat []
  (letsubs [{:keys [group-chat public? input-text]} [:get-current-chat]
            show-bottom-info? [:get-current-chat-ui-prop :show-bottom-info?]
            show-message-options? [:get-current-chat-ui-prop :show-message-options?]
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
      [chat-toolbar public?]
      (when (= :chat current-view)
        [messages-view-animation
         [messages-view group-chat]])
      [input/container {:text-empty? (string/blank? input-text)}]
      (when show-bottom-info?
        [bottom-info/bottom-info-view])
      (when show-message-options?
        [message-options/view])
      [connectivity/error-view {:top (get platform/platform-specific :status-bar-default-height)}]]]))
