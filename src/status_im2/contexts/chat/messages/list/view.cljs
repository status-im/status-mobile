(ns status-im2.contexts.chat.messages.list.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [react-native.background-timer :as background-timer]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [status-im.ui.screens.chat.group :as chat.group]
            [status-im.ui.screens.chat.message.gap :as message.gap]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.composer.utils :as utils]
            [status-im2.contexts.chat.messages.content.deleted.view :as content.deleted]
            [status-im2.contexts.chat.messages.content.view :as message]
            [status-im2.contexts.chat.messages.list.state :as state]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.composer.constants :as composer.constants]))

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
  [{:keys [type value deleted? deleted-for-me? content-type] :as message-data} _ _
   {:keys [context keyboard-shown]}]
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

(defn calc-shell-position
  [y {:keys [input-content-height focused?]} reply edit images]
  (let [lines (utils/calc-lines input-content-height)
        base  (if reply (- composer.constants/reply-container-height) 0)
        base  (if edit (- composer.constants/edit-container-height) base)
        base  (if (seq images) (- composer.constants/images-container-height) base)]
    (if (not focused?)
      (if (> lines 1) (+ -18 base) base)
      (if (> lines 12)
        (reanimated/get-shared-value y)
        (if (> lines 1) (- (- input-content-height composer.constants/input-height base)) base)))))

(defn shell-button
  [insets]
  (let [y              (reanimated/use-shared-value 0)
        chat-input     (rf/sub [:chats/current-chat-input])
        reply          (rf/sub [:chats/reply-message])
        edit           (rf/sub [:chats/edit-message])
        images         (rf/sub [:chats/sending-image])
        shell-position (calc-shell-position y chat-input reply edit images)]
    (rn/use-effect (fn []
                     (reanimated/animate y shell-position))
                   [shell-position])
    [reanimated/view
     {:style (reanimated/apply-animations-to-style
              {:transform [{:translate-y y}]}
              {:bottom   (+ composer.constants/composer-default-height (:bottom insets) 6)
               :position :absolute
               :left     0
               :right    0})}
     [quo/floating-shell-button
      (merge {:jump-to
              {:on-press (fn []
                           (rf/dispatch [:chat/close true])
                           (rf/dispatch [:shell/navigate-to-jump-to]))
               :label    (i18n/label :t/jump-to)
               :style    {:align-self :center}}}
             (when @show-floating-scroll-down-button
               {:scroll-to-bottom {:on-press scroll-to-bottom}}))
      {}]]))

(defn messages-list-content
  [{:keys [chat-id] :as chat} insets keyboard-shown]
  (fn []
    (let [context  (rf/sub [:chats/current-chat-message-list-view-context])
          messages (rf/sub [:chats/raw-chat-messages-stream chat-id])]
      [rn/view
       {:style {:flex 1}}
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
         :content-container-style      {:padding-top    (+ composer.constants/composer-default-height
                                                           (:bottom insets)
                                                           32)
                                        :padding-bottom 16}
         :scroll-indicator-insets      {:top (+ composer.constants/composer-default-height
                                                (:bottom insets))}
         :keyboard-dismiss-mode        :interactive
         :keyboard-should-persist-taps :handled
         :on-momentum-scroll-begin     state/start-scrolling
         :on-momentum-scroll-end       state/stop-scrolling
         :scroll-event-throttle        16
         :on-scroll                    on-scroll
         ;; TODO https://github.com/facebook/react-native/issues/30034
         :inverted                     (when platform/ios? true)
         :style                        (when platform/android? {:scaleY -1})
         :on-layout                    on-messages-view-layout}]
       [:f> shell-button insets]])))

;; This should be replaced with keyboard hook. It has to do with flat-list probably. The keyboard-shown
;; value
;; updates in the parent component, but does not get passed to the children.
;; When using listeners and resetting the value on an atom it works.
(defn use-keyboard-visibility
  []
  (let [show-listener (atom nil)
        hide-listener (atom nil)
        shown?        (atom nil)]
    (rn/use-effect
     (fn []
       (reset! show-listener
         (.addListener rn/keyboard "keyboardWillShow" #(reset! shown? true)))
       (reset! hide-listener
         (.addListener rn/keyboard "keyboardWillHide" #(reset! shown? false)))
       (fn []
         (.remove ^js @show-listener)
         (.remove ^js @hide-listener))))
    {:shown? shown?}))

(defn- f-messages-list
  [chat insets]
  (let [{keyboard-shown? :shown?} (use-keyboard-visibility)]
    [messages-list-content chat insets keyboard-shown?]))

(defn messages-list
  [chat insets]
  [:f> f-messages-list chat insets])
