(ns status-im.ui2.screens.chat.messages.view
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.constants :as constants]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.ui.components.list.views :as list]
            [status-im.ui2.screens.chat.messages.message :as message]
            [status-im.ui.screens.chat.group :as chat.group]
            [status-im.ui.screens.chat.message.datemark :as message-datemark]
            [status-im.ui.screens.chat.message.gap :as gap]
            [status-im.ui.screens.chat.components.messages-skeleton :as messages-skeleton]
            [status-im.utils.utils :as utils]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.chat.state :as state]
            [status-im.ui.components.icons.icons :as icons]))

(defonce ^:const threshold-percentage-to-show-floating-scroll-down-button 75)

(defonce show-floating-scroll-down-button (reagent/atom false))
(defonce messages-list-ref (atom nil))

(def messages-view-height (reagent/atom 0))

(defn on-messages-view-layout [^js ev]
  (reset! messages-view-height (-> ev .-nativeEvent .-layout .-height)))

(def list-key-fn #(or (:message-id %) (:value %)))
(def list-ref #(reset! messages-list-ref %))

(defn scroll-to-bottom []
  (some-> ^js @messages-list-ref (.scrollToOffset #js {:y 0 :animated true})))

(defn floating-scroll-down-button [show-input?]
  [rn/touchable-without-feedback
   {:on-press scroll-to-bottom}
   [rn/view {:style {:position         :absolute
                     :bottom           (if show-input? 126 12)
                     :right            12
                     :height           24
                     :width            24
                     :align-items      :center
                     :justify-content  :center
                     :border-radius    (/ 24 2)
                     :background-color (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70)}}
    ;;TODO icon from quo2 should be used instead!
    [icons/icon
     :main-icons/arrow-down {:color  (colors/theme-colors colors/white colors/neutral-100)
                             :width  12
                             :height 12}]]])

(defn on-scroll [^js ev]
  (let [y (-> ev .-nativeEvent .-contentOffset .-y)
        layout-height (-> ev .-nativeEvent .-layoutMeasurement .-height)
        threshold-height (* (/ layout-height 100) threshold-percentage-to-show-floating-scroll-down-button)
        reached-threshold? (> y threshold-height)]
    (when (not= reached-threshold? @show-floating-scroll-down-button)
      (rn/configure-next (:ease-in-ease-out rn/layout-animation-presets))
      (reset! show-floating-scroll-down-button reached-threshold?))))

(defn chat-intro-header-container
  []
  ;;not implemented
  [rn/view])

(defn list-footer [{:keys [chat-id] :as chat}]
  (let [loading-messages? (<sub [:chats/loading-messages? chat-id])
        no-messages? (<sub [:chats/chat-no-messages? chat-id])
        all-loaded? (<sub [:chats/all-loaded? chat-id])]
    [rn/view {:style (when platform/android? {:scaleY -1})}
     (if (or loading-messages? (not chat-id) (not all-loaded?))
       [messages-skeleton/messages-skeleton @messages-view-height]
       [chat-intro-header-container chat no-messages?])]))

(defn list-header [{:keys [chat-id chat-type invitation-admin]}]
  (when (= chat-type constants/private-group-chat-type)
    [rn/view {:style (when platform/android? {:scaleY -1})}
     [chat.group/group-chat-footer chat-id invitation-admin]]))

(defn render-fn [{:keys [outgoing type] :as message}
                 idx
                 _
                 {:keys [group-chat public? community? current-public-key
                         chat-id show-input? message-pin-enabled edit-enabled in-pinned-view?]}]
  [rn/view {:style (when (and platform/android? (not in-pinned-view?)) {:scaleY -1})}
   (if (= type :datemark)
     [message-datemark/chat-datemark (:value message)]
     (if (= type :gap)
       [gap/gap message idx messages-list-ref false chat-id]
       ; message content
       [message/chat-message
        (assoc message
               :incoming-group (and group-chat (not outgoing))
               :group-chat group-chat
               :public? public?
               :community? community?
               :current-public-key current-public-key
               :show-input? show-input?
               :message-pin-enabled message-pin-enabled
               :edit-enabled edit-enabled)]))])

(defn on-viewable-items-changed [^js e]
  (when @messages-list-ref
    (reset! state/first-not-visible-item
            (when-let [^js last-visible-element (aget (.-viewableItems e) (dec (.-length ^js (.-viewableItems e))))]
              (let [index (.-index last-visible-element)
                    ;; Get first not visible element, if it's a datemark/gap
                    ;; we might unnecessarely add messages on receiving as
                    ;; they do not have a clock value, but most of the times
                    ;; it will be a message
                    first-not-visible (aget (.-data ^js (.-props ^js @messages-list-ref)) (inc index))]
                (when (and first-not-visible
                           (= :message (:type first-not-visible)))
                  first-not-visible))))))

;;TODO this is not really working in pair with inserting new messages because we stop inserting new messages
;;if they outside the viewarea, but we load more here because end is reached,so its slowdown UI because we
;;load and render 20 messages more, but we can't prevent this , because otherwise :on-end-reached will work wrong
(defn list-on-end-reached []
  (if @state/scrolling
    (>evt [:chat.ui/load-more-messages-for-current-chat])
    (utils/set-timeout #(>evt [:chat.ui/load-more-messages-for-current-chat])
                       (if platform/low-device? 700 200))))

(defn get-render-data [{:keys [group-chat chat-id public? community-id admins space-keeper show-input? edit-enabled in-pinned-view?]}]
  (let [current-public-key (<sub [:multiaccount/public-key])
        community (<sub [:communities/community community-id])
        group-admin? (get admins current-public-key)
        community-admin? (when community (community :admin))
        message-pin-enabled (and (not public?)
                                 (or (not group-chat)
                                     (and group-chat
                                          (or group-admin?
                                              community-admin?))))]
    {:group-chat          group-chat
     :public?             public?
     :community?          (not (nil? community-id))
     :current-public-key  current-public-key
     :space-keeper        space-keeper
     :chat-id             chat-id
     :show-input?         show-input?
     :message-pin-enabled message-pin-enabled
     :edit-enabled        edit-enabled
     :in-pinned-view?     in-pinned-view?}))

(defn messages-view [{:keys [chat
                             bottom-space
                             pan-responder
                             mutual-contact-requests-enabled?
                             show-input?]}]
  (let [{:keys [group-chat chat-type chat-id public? community-id admins]} chat
        messages (<sub [:chats/raw-chat-messages-stream chat-id])
        one-to-one? (= chat-type constants/one-to-one-chat-type)
        contact-added? (when one-to-one? (<sub [:contacts/contact-added? chat-id]))
        should-send-contact-request?
        (and
         mutual-contact-requests-enabled?
         one-to-one?
         (not contact-added?))]
    [:<>
     ;;do not use anonymous functions for handlers
     [list/flat-list
      (merge
       pan-responder
       {:key-fn                       list-key-fn
        :ref                          list-ref
        :header                       [list-header chat]
        :footer                       [list-footer chat]
        :data                         (when-not should-send-contact-request?
                                        messages)
        :render-data                  (get-render-data {:group-chat      group-chat
                                                        :chat-id         chat-id
                                                        :public?         public?
                                                        :community-id    community-id
                                                        :admins          admins
                                                        :show-input?     show-input?
                                                        :edit-enabled    true
                                                        :in-pinned-view? false})
        :render-fn                    render-fn
        :on-viewable-items-changed    on-viewable-items-changed
        :on-end-reached               list-on-end-reached
        :on-scroll-to-index-failed    identity              ;;don't remove this
        :content-container-style      {:padding-top    (+ bottom-space 16)
                                       :padding-bottom 16}
        :scroll-indicator-insets      {:top bottom-space}   ;;ios only
        :keyboard-dismiss-mode        :interactive
        :keyboard-should-persist-taps :handled
        :onMomentumScrollBegin        state/start-scrolling
        :onMomentumScrollEnd          state/stop-scrolling
        :scrollEventThrottle          16
        :on-scroll                    on-scroll
        ;;TODO https://github.com/facebook/react-native/issues/30034
        :inverted                     (when platform/ios? true)
        :style                        (when platform/android? {:scaleY -1})
        :on-layout                    on-messages-view-layout})]
     (when @show-floating-scroll-down-button
       [floating-scroll-down-button show-input?])]))
