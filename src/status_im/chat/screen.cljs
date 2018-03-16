(ns status-im.chat.screen
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.chat.styles.screen :as style]
            [status-im.utils.platform :as platform]
            [status-im.chat.views.toolbar-content :as toolbar-content]
            [status-im.chat.views.message.message :as message]
            [status-im.chat.views.message.datemark :as message-datemark]
            [status-im.chat.views.input.input :as input]
            [status-im.chat.views.actions :as actions]
            [status-im.chat.views.bottom-info :as bottom-info]
            [status-im.chat.views.message.datemark :as message-datemark]
            [status-im.chat.views.message.message :as message]
            [status-im.chat.views.input.input :as input]
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

(defn toolbar-action [chat-id chat-name group-chat public?]
  [react/touchable-highlight
   {:on-press            #(list-selection/show {:title chat-name
                                                :options (actions/actions chat-id group-chat public?)})
    :accessibility-label :chat-menu}
   [react/view style/action
    [vector-icons/icon :icons/dots-horizontal]]])

(defview add-contact-bar []
  (letsubs [chat-id          [:get-current-chat-id]
            pending-contact? [:current-contact :pending?]]
    (when (or (nil? pending-contact?) ; user not in contact list
              pending-contact?)
      [react/touchable-highlight
       {:on-press #(re-frame/dispatch [:add-contact chat-id])}
       [react/view style/add-contact
        [react/text {:style style/add-contact-text}
         (i18n/label :t/add-to-contacts)]]])))

(defn- on-options [chat-id chat-name group-chat? public?]
  (list-selection/show {:title   (if public? (str "#" chat-name) chat-name)
                        :options (actions/actions group-chat? chat-id public?)}))

(defview chat-toolbar [public?]
  (letsubs [{:keys [group-chat name chat-id]} [:get-current-chat]]
    [react/view
     [status-bar/status-bar]
     [toolbar/platform-agnostic-toolbar {}
      toolbar/nav-back-count
      [toolbar-content/toolbar-content-view]
      [toolbar/actions [{:icon      :icons/options
                         :icon-opts {:color :black}
                         :handler   #(on-options chat-id name group-chat public?)}]]]
     (when-not (or public? group-chat) [add-contact-bar])]))

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
                                                          :duration duration})])))}
    [react/with-activity-indicator
     {:style   style/message-view-preview
      :preview [react/view style/message-view-preview]}
     [react/animated-view {:style (style/message-view-animated opacity)}
      message-view]]))

(defview messages-view [group-chat]
  (letsubs [messages           [:get-current-chat-messages]
            current-public-key [:get-current-public-key]]
    (if (empty? messages)
      [react/view style/empty-chat-container
       [react/text {:style style/empty-chat-text}
        (i18n/label :t/empty-chat-description)]]
      [list/flat-list {:data                      messages
                       :render-fn                 (fn [{:keys [message-id] :as message}]
                                                    ^{:key message-id}
                                                    [message-row {:group-chat         group-chat
                                                                  :current-public-key current-public-key
                                                                  :row                message}])
                       :inverted                  true
                       :onEndReached              #(re-frame/dispatch [:load-more-messages])
                       :enableEmptySections       true
                       :keyboardShouldPersistTaps (if platform/android? :always :handled)}])))

(defview chat []
  (letsubs [{:keys [group-chat public? input-text]} [:get-current-chat]
            show-bottom-info? [:get-current-chat-ui-prop :show-bottom-info?]
            current-view      [:get :view-id]]
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
     [connectivity/error-view {:top (get platform/platform-specific :status-bar-default-height)}]]))
