(ns status-im.contexts.chat.messenger.messages.list.view
  (:require
    [legacy.status-im.ui.screens.chat.group :as chat.group]
    [oops.core :as oops]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.background-timer :as background-timer]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im.common.home.actions.view :as home.actions]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.composer.constants :as composer.constants]
    [status-im.contexts.chat.messenger.messages.constants :as messages.constants]
    [status-im.contexts.chat.messenger.messages.content.view :as message]
    [status-im.contexts.chat.messenger.messages.list.state :as state]
    [status-im.contexts.chat.messenger.messages.list.style :as style]
    [status-im.contexts.shell.jump-to.constants :as jump-to.constants]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.worklets.chat.messenger.messages :as worklets]))

(def ^:const distance-from-last-message 4)
(def ^:const loading-indicator-page-loading-height 100)

(defn list-key-fn [{:keys [message-id value]}] (or message-id value))
(defn list-ref [ref] (reset! state/messages-list-ref ref))

(defn on-viewable-items-changed
  [e]
  (when @state/messages-list-ref
    (reset! state/first-not-visible-item
      (when-let [last-visible-element (aget (oops/oget e "viewableItems")
                                            (dec (oops/oget e "viewableItems.length")))]
        (let [index             (oops/oget last-visible-element "index")
              ;; Get first not visible element, if it's a datemark/gap
              ;; we might add messages on receiving as they do not have
              ;; a clock value, but usually it will be a message
              first-not-visible (aget (oops/oget @state/messages-list-ref "props.data") (inc index))]
          (when (and first-not-visible
                     (= :message (:type first-not-visible)))
            first-not-visible))))))

(defn list-on-end-reached
  [distance-from-list-top]
  ;; FIXME: that's a bit of a hack but we need to update `distance-from-list-top` once the new
  ;; messages are fetched in order for the header to work properly
  (let [on-loaded (fn [n]
                    (reanimated/set-shared-value distance-from-list-top
                                                 (+ (reanimated/get-shared-value distance-from-list-top)
                                                    (* n 200))))]
    (if @state/scrolling
      (rf/dispatch [:chat.ui/load-more-messages-for-current-chat on-loaded])
      (background-timer/set-timeout #(rf/dispatch [:chat.ui/load-more-messages-for-current-chat
                                                   on-loaded])
                                    (if platform/low-device? 700 100)))))

(defn loading-view
  [{:keys [chat-id window-height]}]
  (let [messages            (rf/sub [:chats/raw-chat-messages-stream chat-id])
        loading-first-page? (= (count messages) 0)
        top-spacing         (if loading-first-page?
                              0
                              (+ messages.constants/top-bar-height (safe-area/get-top)))
        parent-height       (if loading-first-page?
                              window-height
                              loading-indicator-page-loading-height)]
    [rn/view {:padding-top top-spacing}
     ;; Don't use animated loading skeleton https://github.com/status-im/status-mobile/issues/17426
     [quo/skeleton-list
      {:content       :messages
       :parent-height parent-height
       :animated?     false}]]))

(defn header-height
  [{:keys [insets able-to-send-message? images reply edit link-previews? input-content-height]}]
  (if able-to-send-message?
    (cond-> composer.constants/composer-default-height
      (ff/enabled? ::ff/shell.jump-to)
      (+ jump-to.constants/floating-shell-button-height)

      (seq images)
      (+ composer.constants/images-container-height)

      reply
      (+ composer.constants/reply-container-height)

      edit
      (+ composer.constants/edit-container-height)

      link-previews?
      (+ composer.constants/links-container-height)

      (and input-content-height (not= input-content-height composer.constants/input-height))
      (+ composer.constants/input-height)

      true
      (+ (:bottom insets)))
    (- 70 (:bottom insets))))

(defn actions
  [chat-id cover-bg-color]
  (let [latest-pin-text                      (rf/sub [:chats/last-pinned-message-text chat-id])
        pins-count                           (rf/sub [:chats/pin-messages-count chat-id])
        {:keys [muted muted-till chat-type]} (rf/sub [:chats/chat-by-id chat-id])
        community-channel?                   (= constants/community-chat-type chat-type)
        muted?                               (and muted (some? muted-till))
        mute-chat-label                      (if community-channel? :t/mute-channel :t/mute-chat)
        unmute-chat-label                    (if community-channel? :t/unmute-channel :t/unmute-chat)]
    [quo/channel-actions
     {:actions
      [{:accessibility-label :action-button-pinned
        :big?                true
        :label               (if (pos? pins-count) latest-pin-text (i18n/label :t/no-pinned-messages))
        :customization-color cover-bg-color
        :icon                :i/pin
        :counter-value       pins-count
        :on-press            (fn []
                               (rf/dispatch [:pin-message/show-pins-bottom-sheet
                                             chat-id]))}
       {:accessibility-label :action-button-mute
        :label               (i18n/label (if muted
                                           unmute-chat-label
                                           mute-chat-label))
        :customization-color cover-bg-color
        :icon                (if muted? :i/activity-center :i/muted)
        :on-press            (fn []
                               (if muted?
                                 (home.actions/unmute-chat-action chat-id)
                                 (home.actions/mute-chat-action chat-id
                                                                chat-type
                                                                muted?)))}]}]))

(defn more-messages-loader
  [{:keys [chat-id] :as props}]
  (let [loading-messages? (rf/sub [:chats/loading-messages? chat-id])
        all-loaded?       (rf/sub [:chats/all-loaded? chat-id])]
    (when (or loading-messages? (not all-loaded?))
      [loading-view props])))

(defn list-group-chat-header
  [{:keys [chat-id invitation-admin]}]
  [chat.group/group-chat-footer chat-id invitation-admin])

(defn render-fn
  [{:keys [type value] :as message-data} _ _
   {:keys [context keyboard-shown?]}]
  (cond
    (= type :datemark)
    [quo/divider-date value]

    :else
    [message/message message-data context keyboard-shown?]))

(defn on-content-size-change
  [{:keys [content-height distance-atom distance-from-list-top]}]
  (fn [_ content-height-new]
    ;; Updates to shared values are asynchronous and give the wrong value when accessed
    ;; simultaneously(in on-layout event), that's why we are using distance atom here
    (let [change       (- content-height-new @content-height)
          new-distance (+ @distance-atom change)]
      (when-not (= change 0)
        (reanimated/set-shared-value distance-from-list-top new-distance)
        (reset! distance-atom new-distance)
        (reset! content-height content-height-new)))))

(defn on-layout
  [{:keys [event layout-height distance-atom distance-from-list-top on-layout-done?]}]
  (let [layout-height-new (oops/oget event "nativeEvent.layout.height")
        change            (- layout-height-new @layout-height)
        new-distance      (- @distance-atom change)]
    (when (and (not= change 0) (not= @layout-height layout-height-new))
      (reanimated/set-shared-value distance-from-list-top new-distance)
      (reset! distance-atom new-distance)
      (reset! layout-height layout-height-new))
    (reset! on-layout-done? true)))

(defn on-scroll-fn
  [distance-atom layout-height-atom]
  (fn [layout-height new-distance]
    (when (not= layout-height @layout-height-atom)
      (reset! layout-height-atom layout-height))
    (reset! distance-atom new-distance)))

(defn messages-list-content
  [{:keys [insets distance-from-list-top layout-height chat-list-scroll-y on-layout-done?]}]
  (let [content-height           (rn/use-ref-atom 0)
        distance-atom            (rn/use-ref-atom 0)
        theme                    (quo.theme/use-theme)
        {:keys [keyboard-shown]} (hooks/use-keyboard)
        {:keys [chat-type chat-id]
         :as   chat}             (rf/sub [:chats/current-chat-chat-view])
        community-channel?       (= constants/community-chat-type chat-type)
        {window-height :height}  (rn/get-window)
        context                  (rf/sub [:chats/current-chat-message-list-view-context])
        able-to-send-message?    (:able-to-send-message? context)
        messages                 (rf/sub [:chats/raw-chat-messages-stream chat-id])
        margin-bottom?           (and community-channel? (not able-to-send-message?))
        recording?               (rf/sub [:chats/recording?])
        top-margin               (+ (safe-area/get-top) messages.constants/top-bar-height)]
    [rn/view {:style (style/permission-context-sheet margin-bottom?)}
     [rn/view {:style {:flex-shrink 1}} ;; Keeps flat list on top
      [reanimated/flat-list
       {:key-fn                            list-key-fn
        :ref                               list-ref
        :bounces                           false
        :header                            (when (= (:chat-type chat) constants/private-group-chat-type)
                                             [list-group-chat-header chat])
        :footer                            [more-messages-loader
                                            {:chat-id       chat-id
                                             :window-height window-height}]
        :data                              messages
        :render-data                       {:theme           theme
                                            :context         context
                                            :keyboard-shown? keyboard-shown
                                            :insets          insets}
        :render-fn                         render-fn
        :on-viewable-items-changed         on-viewable-items-changed
        :on-content-size-change            (on-content-size-change
                                            {:content-height         content-height
                                             :distance-atom          distance-atom
                                             :distance-from-list-top distance-from-list-top})
        :on-end-reached                    #(list-on-end-reached distance-from-list-top)
        :on-scroll-to-index-failed         identity
        :keyboard-dismiss-mode             :interactive
        :keyboard-should-persist-taps      :always
        :on-scroll-begin-drag              (fn []
                                             (rf/dispatch [:chat.ui/set-input-focused false])
                                             (rn/dismiss-keyboard!))
        :on-momentum-scroll-begin          state/start-scrolling
        :on-momentum-scroll-end            state/stop-scrolling
        :scroll-event-throttle             16
        :on-scroll                         (reanimated/use-animated-scroll-handler
                                            (worklets/messages-list-on-scroll
                                             distance-from-list-top
                                             chat-list-scroll-y
                                             (on-scroll-fn distance-atom layout-height)))
        :style                             {:background-color (colors/theme-colors colors/white
                                                                                   colors/neutral-95
                                                                                   theme)}
        :content-container-style           {:padding-top    distance-from-last-message
                                            :padding-bottom top-margin}
        :inverted                          true
        :on-layout                         #(on-layout
                                             {:event                  %
                                              :layout-height          layout-height
                                              :distance-atom          distance-atom
                                              :distance-from-list-top distance-from-list-top
                                              :on-layout-done?        on-layout-done?})
        :scroll-enabled                    (not recording?)
        :content-inset-adjustment-behavior :never
        :scroll-indicator-insets           {:right 1}}]]]))
