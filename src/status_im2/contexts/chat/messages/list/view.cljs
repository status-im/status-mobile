(ns status-im2.contexts.chat.messages.list.view
  (:require
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
    [status-im.ui.screens.chat.group :as chat.group]
    [status-im2.common.home.actions.view :as home.actions]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.composer.constants :as composer.constants]
    [status-im2.contexts.chat.messages.constants :as messages.constants]
    [status-im2.contexts.chat.messages.content.view :as message]
    [status-im2.contexts.chat.messages.list.state :as state]
    [status-im2.contexts.chat.messages.list.style :as style]
    [status-im2.contexts.shell.jump-to.constants :as jump-to.constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.worklets.chat.messages :as worklets]))

(defonce ^:const threshold-percentage-to-show-floating-scroll-down-button 75)
(defonce ^:const loading-indicator-extra-spacing 250)
(defonce ^:const loading-indicator-page-loading-height 100)
(defonce ^:const min-message-height 32)
(defonce messages-list-ref (atom nil))

(defn list-key-fn [{:keys [message-id value]}] (or message-id value))
(defn list-ref [ref] (reset! messages-list-ref ref))

(defn scroll-to-bottom
  []
  (some-> ^js @messages-list-ref
          (.scrollToOffset #js
                            {:animated true})))

(defn on-scroll-fn
  [show-floating-scroll-down-button?]
  (fn [y layout-height]
    (let [threshold-height   (* (/ layout-height 100)
                                threshold-percentage-to-show-floating-scroll-down-button)
          reached-threshold? (> y threshold-height)]
      (when (not= reached-threshold? @show-floating-scroll-down-button?)
        (rn/configure-next (:ease-in-ease-out rn/layout-animation-presets))
        (reset! show-floating-scroll-down-button? reached-threshold?)))))

(defn on-viewable-items-changed
  [e]
  (when @messages-list-ref
    (reset! state/first-not-visible-item
      (when-let [last-visible-element (aget (oops/oget e "viewableItems")
                                            (dec (oops/oget e "viewableItems.length")))]
        (let [index             (oops/oget last-visible-element "index")
              ;; Get first not visible element, if it's a datemark/gap
              ;; we might add messages on receiving as they do not have
              ;; a clock value, but usually it will be a message
              first-not-visible (aget (oops/oget @messages-list-ref "props.data") (inc index))]
          (when (and first-not-visible
                     (= :message (:type first-not-visible)))
            first-not-visible))))))

(defn list-on-end-reached
  [distance-from-list-top]
  ;; FIXME: that's a bit of a hack but we need to update `distance-from-list-top` once the new messages
  ;; are fetched in order for the header to work properly
  (let [on-loaded (fn [n]
                    (reanimated/set-shared-value distance-from-list-top
                                                 (+ (reanimated/get-shared-value distance-from-list-top)
                                                    (* n 200))))]
    (if @state/scrolling
      (rf/dispatch [:chat.ui/load-more-messages-for-current-chat on-loaded])
      (background-timer/set-timeout #(rf/dispatch [:chat.ui/load-more-messages-for-current-chat
                                                   on-loaded])
                                    (if platform/low-device? 700 100)))))

(defn- contact-icon
  [{:keys [ens-verified added?]} theme]
  (when (or ens-verified added?)
    [rn/view
     {:style {:margin-left 4
              :margin-top  8}}
     (if ens-verified
       [quo/icon :i/verified
        {:no-color true
         :size     20
         :color    (colors/theme-colors
                    (colors/custom-color :success 50)
                    (colors/custom-color :success 60)
                    theme)}]
       (when added?
         [quo/icon :i/contact
          {:no-color true
           :size     20
           :color    (colors/theme-colors colors/primary-50 colors/primary-60 theme)}]))]))

(defn- skeleton-list-props
  [content parent-height animated?]
  {:content       content
   :parent-height parent-height
   :animated?     animated?})

(defn loading-view
  [chat-id {:keys [window-height]}]
  (let [messages            (rf/sub [:chats/raw-chat-messages-stream chat-id])
        loading-first-page? (= (count messages) 0)
        top-spacing         (if loading-first-page?
                              0
                              (+ messages.constants/top-bar-height (safe-area/get-top)))
        parent-height       (if loading-first-page?
                              window-height
                              loading-indicator-page-loading-height)]
    [rn/view {:padding-top top-spacing}
     ;; Only use animated loading skeleton for ios
     ;; https://github.com/status-im/status-mobile/issues/17426
     [quo/skeleton-list (skeleton-list-props :messages parent-height platform/ios?)]]))

(defn list-header
  [insets able-to-send-message?]
  (let [images (rf/sub [:chats/sending-image])
        height (if able-to-send-message?
                 (+ composer.constants/composer-default-height
                    jump-to.constants/floating-shell-button-height
                    (if (seq images) composer.constants/images-container-height 0)
                    (:bottom insets))
                 (- 70 (:bottom insets)))]
    [rn/view {:style {:height height}}]))

(defn f-list-footer-avatar
  [{:keys [distance-from-list-top display-name online? profile-picture theme]}]
  (let [scale (reanimated/interpolate distance-from-list-top
                                      [0 messages.constants/header-container-top-margin]
                                      [1 0.4]
                                      messages.constants/default-extrapolation-option)
        top   (reanimated/interpolate distance-from-list-top
                                      [0 messages.constants/header-container-top-margin]
                                      [-40 -8]
                                      messages.constants/default-extrapolation-option)
        left  (reanimated/interpolate distance-from-list-top
                                      [0 messages.constants/header-container-top-margin]
                                      [20 -4]
                                      messages.constants/default-extrapolation-option)]
    [reanimated/view
     {:style (style/header-image scale top left theme)}
     [quo/user-avatar
      {:full-name       display-name
       :online?         online?
       :profile-picture profile-picture
       :size            :big}]]))

(defn actions
  [chat-id cover-bg-color]
  (let [latest-pin-text                      (rf/sub [:chats/last-pinned-message-text chat-id])
        pins-count                           (rf/sub [:chats/pin-messages-count chat-id])
        {:keys [muted muted-till chat-type]} (rf/sub [:chat-by-id chat-id])
        community-channel?                   (= constants/community-chat-type chat-type)
        muted?                               (and muted (some? muted-till))
        mute-chat-label                      (if community-channel? :t/mute-channel :t/mute-chat)
        unmute-chat-label                    (if community-channel? :t/unmute-channel :t/unmute-chat)]
    [quo/channel-actions
     {:style   {:margin-top 16}
      :actions [{:accessibility-label :action-button-pinned
                 :big?                true
                 :label               (or latest-pin-text (i18n/label :t/no-pinned-messages))
                 :color               cover-bg-color
                 :icon                :i/pin
                 :counter-value       pins-count
                 :on-press            (fn []
                                        (rf/dispatch [:dismiss-keyboard])
                                        (rf/dispatch [:pin-message/show-pins-bottom-sheet
                                                      chat-id]))}
                {:accessibility-label :action-button-mute
                 :label               (i18n/label (if muted
                                                    unmute-chat-label
                                                    mute-chat-label))
                 :color               cover-bg-color
                 :icon                (if muted? :i/muted :i/activity-center)
                 :on-press            (fn []
                                        (if muted?
                                          (home.actions/unmute-chat-action chat-id)
                                          (home.actions/mute-chat-action chat-id
                                                                         chat-type
                                                                         muted?)))}]}]))

(defn f-list-footer
  [{:keys [chat distance-from-list-top cover-bg-color theme]}]
  (let [{:keys [chat-id chat-name emoji chat-type
                group-chat]} chat
        display-name         (cond
                               (= chat-type constants/one-to-one-chat-type)
                               (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
                               (= chat-type constants/community-chat-type)
                               (str (when emoji (str emoji " ")) "# " chat-name)
                               :else (str emoji chat-name))
        {:keys [bio]}        (rf/sub [:contacts/contact-by-identity chat-id])
        online?              (rf/sub [:visibility-status-updates/online? chat-id])
        contact              (when-not group-chat
                               (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path           (rf/sub [:chats/photo-path chat-id])
        top-margin           (+ (safe-area/get-top)
                                messages.constants/top-bar-height
                                messages.constants/header-container-top-margin)
        background-color     (colors/theme-colors
                              (colors/custom-color cover-bg-color 50 20)
                              (colors/custom-color cover-bg-color 50 40)
                              theme)
        border-radius        (reanimated/interpolate
                              distance-from-list-top
                              [0 messages.constants/header-container-top-margin]
                              [20 0]
                              messages.constants/default-extrapolation-option)
        background-opacity   (reanimated/interpolate
                              distance-from-list-top
                              [messages.constants/header-container-top-margin
                               (+ messages.constants/header-animation-distance
                                  messages.constants/header-container-top-margin)]
                              [1 0]
                              messages.constants/default-extrapolation-option)]
    [:<>
     [reanimated/view
      {:style (style/background-container background-color background-opacity top-margin)}]
     [reanimated/view {:style (style/header-bottom-part border-radius theme top-margin)}
      (when-not group-chat
        [:f> f-list-footer-avatar
         {:distance-from-list-top distance-from-list-top
          :display-name           display-name
          :online?                online?
          :theme                  theme
          :profile-picture        photo-path}])
      [rn/view
       {:style {:flex-direction :row
                :margin-top     (if group-chat 94 52)}}
       [quo/text
        {:weight          :semi-bold
         :size            :heading-1
         :style           {:flex-shrink 1}
         :number-of-lines 1}
        display-name]
       [contact-icon contact theme]]
      (when bio
        [quo/text {:style style/bio}
         bio])
      [actions chat-id cover-bg-color]]]))

(defn list-footer
  [props]
  (let [chat-id           (get-in props [:chat :chat-id])
        loading-messages? (rf/sub [:chats/loading-messages? chat-id])
        all-loaded?       (rf/sub [:chats/all-loaded? chat-id])]
    [:<>
     (if (or loading-messages? (not all-loaded?))
       [loading-view chat-id props]
       [:f> f-list-footer props])]))

(defn list-group-chat-header
  [{:keys [chat-id invitation-admin]}]
  [rn/view
   [chat.group/group-chat-footer chat-id invitation-admin]])

(defn render-fn
  [{:keys [type value content-type] :as message-data} _ _
   {:keys [context keyboard-shown?]}]
  (when (not= content-type constants/content-type-contact-request)
    (cond
      (= type :datemark)
      [quo/divider-date value]

      :else
      [message/message message-data context keyboard-shown?])))

(defn on-content-size-change
  [{:keys [distance-from-list-top window-height content-height calculations-complete?]}]
  (fn [_ content-height-new]
    (let [change   (- content-height-new @content-height)
          distance (if (reanimated/get-shared-value calculations-complete?)
                     (+ (reanimated/get-shared-value distance-from-list-top) change)
                     (- content-height-new window-height))]
      (reanimated/set-shared-value distance-from-list-top distance)
      (reset! content-height content-height-new))
    (when-not (reanimated/get-shared-value calculations-complete?)
      (js/setTimeout #(reanimated/set-shared-value calculations-complete? true) 10))))

(defn keyboard-offset
  [distance-from-list-top keyboard-shown keyboard-height keyboard-offset?]
  ;; Note - keyboard fires multiple events, we are making sure we only offset once
  (when (> keyboard-height 0)
    (let [current-distance-from-top (reanimated/get-shared-value distance-from-list-top)]
      (when (and keyboard-shown (not @keyboard-offset?))
        (reanimated/set-shared-value distance-from-list-top
                                     (+ current-distance-from-top keyboard-height))
        (reset! keyboard-offset? true))
      (when (and (not keyboard-shown) @keyboard-offset?)
        (reanimated/set-shared-value distance-from-list-top
                                     (- current-distance-from-top keyboard-height))
        (reset! keyboard-offset? false)))))

(defn f-messages-list-content
  [{:keys [insets distance-from-list-top keyboard-offset? content-height cover-bg-color
           show-floating-scroll-down-button? calculations-complete?
           messages-list-on-layout-finished?]}]
  (let [theme                                    (quo.theme/use-theme-value)
        chat                                     (rf/sub [:chats/current-chat-chat-view])
        {:keys [keyboard-shown keyboard-height]} (hooks/use-keyboard)
        {window-height :height}                  (rn/get-window)
        context                                  (rf/sub [:chats/current-chat-message-list-view-context])
        messages                                 (rf/sub [:chats/raw-chat-messages-stream
                                                          (:chat-id chat)])
        recording?                               (rf/sub [:chats/recording?])]
    (keyboard-offset distance-from-list-top keyboard-shown keyboard-height keyboard-offset?)
    [rn/view {:style {:flex 3}} ;; Pushes composer to bottom
     [rn/view {:style {:flex-shrink 1}} ;; Keeps flat list on top
      [reanimated/flat-list
       {:key-fn                            list-key-fn
        :ref                               list-ref
        :bounces                           false
        :header                            [:<>
                                            [list-header insets (:able-to-send-message? context)]
                                            (when (= (:chat-type chat) constants/private-group-chat-type)
                                              [list-group-chat-header chat])]
        :footer                            [list-footer
                                            {:theme                  theme
                                             :chat                   chat
                                             :window-height          window-height
                                             :distance-from-list-top distance-from-list-top
                                             :cover-bg-color         cover-bg-color}]
        :data                              messages
        :render-data                       {:theme           theme
                                            :context         context
                                            :keyboard-shown? keyboard-shown
                                            :insets          insets}
        :render-fn                         render-fn
        :on-viewable-items-changed         on-viewable-items-changed
        :on-content-size-change            (on-content-size-change
                                            {:distance-from-list-top distance-from-list-top
                                             :window-height          window-height
                                             :content-height         content-height
                                             :calculations-complete? calculations-complete?})
        :on-end-reached                    #(list-on-end-reached distance-from-list-top)
        :on-scroll-to-index-failed         identity
        :scroll-indicator-insets           {:top (if (:able-to-send-message? context)
                                                   (- composer.constants/composer-default-height 16)
                                                   0)}
        :keyboard-dismiss-mode             :interactive
        :keyboard-should-persist-taps      :always
        :on-scroll-begin-drag              #(do
                                              (rf/dispatch [:chat.ui/set-input-focused false])
                                              (rn/dismiss-keyboard!))
        :on-momentum-scroll-begin          state/start-scrolling
        :on-momentum-scroll-end            state/stop-scrolling
        :scroll-event-throttle             16
        :on-scroll                         (reanimated/use-animated-scroll-handler
                                            (worklets/messages-list-on-scroll
                                             distance-from-list-top
                                             (on-scroll-fn show-floating-scroll-down-button?)))
        :style                             {:background-color (colors/theme-colors colors/white
                                                                                   colors/neutral-95
                                                                                   theme)}
        :inverted                          true
        :on-layout                         (fn [_]
                                             (js/setTimeout #(reset! messages-list-on-layout-finished?
                                                               true)
                                                            1000))
        :scroll-enabled                    (not recording?)
        :content-inset-adjustment-behavior :never}]]]))
