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
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.utils :as utils]))

(defview add-contact-bar [contact-identity]
  (letsubs [{:keys [hide-contact?] :as contact} [:get-contact-by-identity contact-identity]]
    (when (and (not hide-contact?)
               (models.contact/can-add-to-contacts? contact))
      [react/view style/add-contact
       [react/view style/add-contact-left]
       [react/touchable-highlight
        {:on-press            #(re-frame/dispatch [:add-contact contact-identity])
         :accessibility-label :add-to-contacts-button}
        [react/view style/add-contact-center
         [vector-icons/icon :icons/add {:color colors/blue}]
         [react/i18n-text {:style style/add-contact-text :key :add-to-contacts}]]]
       [react/touchable-highlight
        {:on-press            #(re-frame/dispatch [:hide-contact contact-identity])
         :accessibility-label :add-to-contacts-close-button}
        [vector-icons/icon :icons/close {:color           colors/gray-icon
                                         :container-style style/add-contact-close-icon}]]])))

(defn- on-options [chat-id chat-name group-chat? public?]
  (list-selection/show {:title   chat-name
                        :options (actions/actions group-chat? chat-id public?)}))

(defview scroll-to-bottom-button [chat-id list-ref]
  (letsubs [unread-count [:unviewed-messages-count chat-id]
            offset [:get-current-chat-ui-prop :offset]]
    (let [have-unreads? (pos? unread-count)
          many-messages? (< 99 unread-count)]
      (when (or have-unreads?
                (< 100 offset))
        [react/view (style/scroll-to-bottom-button have-unreads? many-messages?)
         [react/touchable-highlight
          {:on-press            (fn []
                                  (when @list-ref
                                    (.scrollToOffset @list-ref #js {:offset 0 :animated false})
                                    (re-frame/dispatch [:chat-scroll-to-end])))
           :accessibility-label :scroll-to-bottom-button}
          [react/view style/scroll-to-bottom-button-inner
           (when have-unreads?
             [react/text {:style style/scroll-to-bottom-button-text}
              (if many-messages? "100+" unread-count)])
           [vector-icons/icon :icons/dropdown-down {:container-style (style/scroll-to-bottom-button-icon have-unreads?)
                                                    :color           colors/white}]]]]))))

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
        [toolbar/actions [{:icon      :icons/wallet
                           :icon-opts {:color               :black
                                       :accessibility-label :wallet-modal-button}
                           :handler   #(re-frame/dispatch [:navigate-to-modal :wallet-modal])}
                          {:icon      :icons/options
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

(defview messages-list [messages group-chat list-ref]
  (letsubs [current-public-key [:get-current-public-key]
            offset [:get-current-chat-ui-prop :offset]]
    {:should-component-update (constantly false)}
    [list/flat-list {:data                      messages
                     :style                     {:opacity 0}
                     :ref                       #(reset! list-ref %)
                     :key-fn                    #(or (:message-id %) (:value %))
                     :render-fn                 (fn [message]
                                                  [message-row {:group-chat         group-chat
                                                                :current-public-key current-public-key
                                                                :row                message}])
                     :inverted                  true
                     :on-scroll                 (fn [_]
                                                  (when (zero? (.. @list-ref -props -style -opacity))
                                                    (.runAfterInteractions js-dependencies/interaction-manager
                                                                           #(.setNativeProps @list-ref #js {:opacity 1}))))
                     :on-layout                 (fn [_]
                                                  (utils/set-timeout
                                                   #(.setNativeProps @list-ref #js {:opacity 1})
                                                   1000))
                     :on-content-size-change    (fn [_]
                                                  (when (zero? (.. @list-ref -props -style -opacity))
                                                    (.scrollToOffset @list-ref #js {:offset offset :animated false})))
                     :onScrollEndDrag           (fn [e]
                                                  (let [offset (.. e -nativeEvent -contentOffset -y)]
                                                    (re-frame/dispatch [:set-chat-ui-props {:offset (max offset 0)}])
                                                    (when (zero? offset)
                                                      (re-frame/dispatch [:chat-scroll-to-end]))))
                     :onMomentumScrollEnd       (fn [e]
                                                  (let [offset (.. e -nativeEvent -contentOffset -y)]
                                                    (re-frame/dispatch [:set-chat-ui-props {:offset (max offset 0)}])
                                                    (when (zero? offset)
                                                      (re-frame/dispatch [:chat-scroll-to-end]))))
                     :onEndReached              #(re-frame/dispatch [:load-more-messages])
                     :enableEmptySections       true
                     :keyboardShouldPersistTaps :handled}]))

(defview messages-view [group-chat list-ref]
  (letsubs [messages           [:get-current-chat-messages-stream]
            chat               [:get-current-chat]]
    {:component-did-mount #(re-frame/dispatch [:set-chat-ui-props {:messages-focused? true
                                                                   :input-focused? false}])}
    (if (empty? messages)
      [empty-chat-container chat]
      [messages-list messages group-chat list-ref])))

(defview chat []
  (letsubs [{:keys [group-chat public? input-text chat-id]} [:get-current-chat]
            show-bottom-info? [:get-current-chat-ui-prop :show-bottom-info?]
            show-message-options? [:get-current-chat-ui-prop :show-message-options?]
            current-view [:get :view-id]
            list-ref (atom nil)]
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
         [messages-view group-chat list-ref]])
      [input/container {:text-empty? (string/blank? input-text)}]
      (when show-bottom-info?
        [bottom-info/bottom-info-view])
      (when show-message-options?
        [message-options/view])
      [connectivity/error-view {:top (get platform/platform-specific :status-bar-default-height)}]
      [scroll-to-bottom-button chat-id list-ref]]]))
