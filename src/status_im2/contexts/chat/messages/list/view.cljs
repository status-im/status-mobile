(ns status-im2.contexts.chat.messages.list.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [react-native.background-timer :as background-timer]
            [react-native.core :as rn]
            [react-native.hooks :as hooks]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [status-im.ui.screens.chat.group :as chat.group]
            [status-im.ui.screens.chat.message.gap :as message.gap]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.messages.content.deleted.view :as content.deleted]
            [status-im2.contexts.chat.messages.content.view :as message]
            [status-im2.contexts.chat.messages.list.state :as state]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defonce messages-list-ref (atom nil))

(defn list-key-fn [{:keys [message-id value]}] (or message-id value))
(defn list-ref [ref] (reset! messages-list-ref ref))

(defn scroll-to-bottom
  []
  (some-> ^js @messages-list-ref
          (.scrollToOffset #js {:y 0 :animated true})))

(defonce ^:const threshold-percentage-to-show-floating-scroll-down-button 75)
(defonce show-floating-scroll-down-button (reagent/atom false))

(defn on-scroll
  [evt]
  (let [y                  (oops/oget evt "nativeEvent.contentOffset.y")
        layout-height      (oops/oget evt "nativeEvent.layoutMeasurement.height")
        threshold-height   (* (/ layout-height 100)
                              threshold-percentage-to-show-floating-scroll-down-button)
        reached-threshold? (> y threshold-height)]
    (when (not= reached-threshold? @show-floating-scroll-down-button)
      (rn/configure-next (:ease-in-ease-out rn/layout-animation-presets))
      (reset! show-floating-scroll-down-button reached-threshold?))))

(defn on-viewable-items-changed
  [evt]
  (when @messages-list-ref
    (reset! state/first-not-visible-item
            (when-let [last-visible-element (aget (oops/oget evt "viewableItems")
                                                  (dec (oops/oget evt "viewableItems.length")))]
              (let [index             (oops/oget last-visible-element "index")
                    ;; Get first not visible element, if it's a datemark/gap
                    ;; we might unnecessarely add messages on receiving as
                    ;; they do not have a clock value, but most of the times
                    ;; it will be a message
                    first-not-visible (aget (oops/oget @messages-list-ref "props.data") (inc index))]
                (when (and first-not-visible
                           (= :message (:type first-not-visible)))
                  first-not-visible))))))

;;TODO this is not really working in pair with inserting new messages because we stop inserting new
;;messages
;;if they outside the viewarea, but we load more here because end is reached,so its slowdown UI because
;;we
;;load and render 20 messages more, but we can't prevent this , because otherwise :on-end-reached will
;;work wrong
(defn list-on-end-reached
  []
  (if @state/scrolling
    (rf/dispatch [:chat.ui/load-more-messages-for-current-chat])
    (background-timer/set-timeout #(rf/dispatch [:chat.ui/load-more-messages-for-current-chat])
                                  (if platform/low-device? 700 200))))

(defonce messages-view-height (reagent/atom 0))

(defn on-messages-view-layout
  [evt]
  (reset! messages-view-height (oops/oget evt "nativeEvent.layout.height")))

(defn list-footer
  [{:keys [chat-id]}]
  (let [loading-messages? (rf/sub [:chats/loading-messages? chat-id])
        all-loaded?       (rf/sub [:chats/all-loaded? chat-id])]
    (when (or loading-messages? (not chat-id) (not all-loaded?))
      [rn/view {:style (when platform/android? {:scaleY -1})}
       [quo/skeleton @messages-view-height]])))

(defn list-header
  [{:keys [chat-id chat-type invitation-admin]}]
  (when (= chat-type constants/private-group-chat-type)
    [rn/view {:style (when platform/android? {:scaleY -1})}
     [chat.group/group-chat-footer chat-id invitation-admin]]))

(defn render-fn
  [{:keys [type value deleted? deleted-for-me? content-type] :as message-data} _ _ {:keys [context keyboard-shown]}]
  [rn/view {:style (when platform/android? {:scaleY -1})}
   (if (= type :datemark)
     [quo/divider-date value]
     (if (= content-type constants/content-type-gap)
       [not-implemented/not-implemented
        [message.gap/gap message-data]]
       [rn/view {:padding-horizontal 8}
        (if (or deleted? deleted-for-me?)
          [content.deleted/deleted-message message-data context]
          [message/message-with-reactions message-data context keyboard-shown])]))])


(defn shell-button
  [insets]
  [:f>
  (fn []
    (let [{:keys [input-content-height focused?]} (rf/sub [:chats/current-chat-input])
          lines      (Math/round (/ input-content-height 22))
          lines      (if platform/ios? lines (dec lines))
          extra      (if (and (not focused?) (> lines 1)) -18 0)
          y (reanimated/use-shared-value 0)]
      (rn/use-effect (fn []
                       (reanimated/animate y extra 100)) [extra])
      [reanimated/view {:style (reanimated/apply-animations-to-style
                                 {:transform [{:translate-y y}]}
                                 {:bottom (+ (if platform/ios? 6 20) (+ 108 (:bottom insets)))
                                  :position    :absolute
                                  :left 0
                                  :right 0})}
      [quo/floating-shell-button
       (merge {:jump-to
               {:on-press #(do
                             (rf/dispatch [:chat/close true])
                             (rf/dispatch [:shell/navigate-to-jump-to]))
                :label    (i18n/label :t/jump-to)
                :style {:align-self :center}}}
              (when @show-floating-scroll-down-button
                {:scroll-to-bottom {:on-press scroll-to-bottom}}))
       {}]]))])

(defn messages-list
  [{:keys [chat-id] :as chat} insets]
  [:f>
   (fn []
     (let [keyboard-show-listener (atom nil)
           keyboard-hide-listener (atom nil)
           keyboard-shown         (atom false)]
       (rn/use-effect
         (fn [] (reset! keyboard-show-listener (.addListener rn/keyboard "keyboardWillShow"
                                                             (fn [e] (reset! keyboard-shown true))))
           (reset! keyboard-hide-listener (.addListener rn/keyboard "keyboardWillHide"
                                                        (fn [e] (reset! keyboard-shown false))))
           (fn []
             (.remove ^js @keyboard-show-listener)
             (.remove ^js @keyboard-hide-listener))))
       [:f>
        (fn []
          (let [context      (rf/sub [:chats/current-chat-message-list-view-context])
                messages     (rf/sub [:chats/raw-chat-messages-stream chat-id])
                bottom-space 15]
            [rn/view
             {:style {:flex 1
                      ;:padding-bottom (+ 108 (:bottom insets))
                      }} ;; TODO: 108 is the composer's min-height
             ;; NOTE: DO NOT use anonymous functions for handlers
             [rn/flat-list
              {:key-fn                       list-key-fn
               :ref                          list-ref
               :header                       [list-header chat]
               :footer                       [list-footer chat]
               :data                         messages
               :render-data                  {:context        context
                                              :keyboard-shown keyboard-shown}
               :render-fn                    render-fn
               :on-viewable-items-changed    on-viewable-items-changed
               :on-end-reached               list-on-end-reached
               :on-scroll-to-index-failed    identity ; don't remove this
               :content-container-style      {:padding-top    (+ 108 (:bottom insets) 32)
                                              :padding-bottom 16}
               :scroll-indicator-insets      {:top (+ 108 (:bottom insets))}
               :keyboard-dismiss-mode        :interactive
               :keyboard-should-persist-taps :handled
               :onMomentumScrollBegin        state/start-scrolling
               :onMomentumScrollEnd          state/stop-scrolling
               :scrollEventThrottle          16
               :on-scroll                    on-scroll
               ;; TODO https://github.com/facebook/react-native/issues/30034
               :inverted                     (when platform/ios? true)
               :style                        (when platform/android? {:scaleY -1})
               :on-layout                    on-messages-view-layout}]

             [shell-button insets]]))]))])
