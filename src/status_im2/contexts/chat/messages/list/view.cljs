(ns status-im2.contexts.chat.messages.list.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [react-native.safe-area :as safe-area]
            [react-native.background-timer :as background-timer]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [react-native.reanimated :as reanimated]
            [status-im.ui.screens.chat.group :as chat.group]
            [status-im.ui.screens.chat.message.gap :as message.gap]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.messages.content.deleted.view :as content.deleted]
            [status-im2.contexts.chat.messages.content.view :as message]
            [status-im2.contexts.chat.messages.list.state :as state]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im.ui.components.fast-image :as fast-image]

            [quo2.components.animated-header-flatlist.style :as style]))

(defonce messages-list-ref (atom nil))

(defonce list-key-fn #(or (:message-id %) (:value %)))
(defonce list-ref #(reset! messages-list-ref %))

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

(defn get-render-data
  "compute data used to render message list, including pinned message list and message list in chats"
  [{:keys [group-chat chat-id public? community-id admins space-keeper show-input? edit-enabled
           in-pinned-view?]}]
  (let [current-public-key                                       (rf/sub [:multiaccount/public-key])
        {:keys [can-delete-message-for-everyone?] :as community} (rf/sub [:communities/community
                                                                          community-id])
        group-admin?                                             (get admins current-public-key)
        community-admin?                                         (when community (community :admin))
        message-pin-enabled                                      (and (not public?)
                                                                      (or (not group-chat)
                                                                          (and group-chat
                                                                               (or group-admin?
                                                                                   community-admin?))))]
    {:group-chat                       group-chat
     :group-admin?                     group-admin?
     :public?                          public?
     :community?                       (not (nil? community-id))
     :current-public-key               current-public-key
     :space-keeper                     space-keeper
     :chat-id                          chat-id
     :show-input?                      show-input?
     :message-pin-enabled              message-pin-enabled
     :edit-enabled                     edit-enabled
     :in-pinned-view?                  in-pinned-view?
     :can-delete-message-for-everyone? can-delete-message-for-everyone?}))

(defonce messages-view-height (reagent/atom 0))

(defn on-messages-view-layout
  [evt]
  (reset! messages-view-height (oops/oget evt "nativeEvent.layout.height")))

;; TODO(alwx): revisit
(def header-height 234)
(def cover-height 192)
(def blur-view-height 100)
(def threshold (- header-height blur-view-height))

(defn header
  [{:keys [theme-color cover-uri cover-bg-color title-comp]} top-inset scroll-y]
  (let [input-range [0 (* threshold 0.33)]
        border-animation (reanimated/interpolate scroll-y input-range [12 0]
                                                 {:extrapolateLeft  "clamp"
                                                  :extrapolateRight "clamp"})]
    [rn/view
     {:style {:background-color (or theme-color (colors/theme-colors colors/white colors/neutral-95))
              :margin-top       (when platform/ios? (- top-inset))}}
     (when cover-uri
       [fast-image/fast-image
        {:style  {:width  "100%"
                  :height cover-height}
         :source {:uri cover-uri}}])
     (when cover-bg-color
       [rn/view
        {:style {:width            "100%"
                 :height           cover-height
                 :background-color cover-bg-color}}])
     [reanimated/view {:style (style/header-bottom-part border-animation)}
      [title-comp]]]))

(defn list-footer
  [{:keys [chat-id chat-name emoji chat-type group-chat]} top-inset scroll-y cover-bg-color]
  (let [display-name        (if (= chat-type constants/one-to-one-chat-type)
                              (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
                              (str emoji " " chat-name))
        loading-messages? (rf/sub [:chats/loading-messages? chat-id])
        all-loaded?       (rf/sub [:chats/all-loaded? chat-id])
        online?           (rf/sub [:visibility-status-updates/online? chat-id])
        contact           (when-not group-chat (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path        (when-not (empty? (:images contact)) (rf/sub [:chats/photo-path chat-id]))]
    [:f>
     (fn []
       (let [_ (js/console.log "ALWX scroll-y" scroll-y)
             border-animation (reanimated/interpolate scroll-y [0 (* threshold 0.33)] [12 0]
                                                      {:extrapolateLeft  "clamp"
                                                       :extrapolateRight "clamp"})]
         [:<>
          [rn/view
           {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
                    :margin-top (when platform/ios? (- top-inset))}}
           #_(when cover-uri
             [fast-image/fast-image
              {:style  {:width  "100%"
                        :height cover-height}
               :source {:uri cover-uri}}])
           (when cover-bg-color
             [rn/view
              {:style {:width            "100%"
                       :height           cover-height
                       :background-color cover-bg-color}}])
           [reanimated/view {:style (style/header-bottom-part border-animation)}
            [rn/view {:style {:margin-top    -36
                              :margin-left   20
                              :margin-right  20
                              :margin-bottom 20}}
             [user-avatar/user-avatar {:full-name       display-name
                                       :online?         online?
                                       :profile-picture photo-path
                                       :size            :big}]
             [quo/text
              {:weight          :semi-bold
               :size            :heading-1
               :style           {:margin-top 12}
               :number-of-lines 1}
              display-name]
             [quo/text {:style {:margin-top 8}}
              "Web 3.0 Designer @ethstatus • DJ • Producer • Dad • YouTuber."]]]]
          (when (or loading-messages? (not chat-id) (not all-loaded?))
            [rn/view {:style (when platform/android? {:scaleY -1})}
             [quo/skeleton @messages-view-height]])]))]))

(defn list-header
  [{:keys [chat-id chat-type invitation-admin]}]
  (when (= chat-type constants/private-group-chat-type)
    [rn/view {:style (when platform/android? {:scaleY -1})}
     [chat.group/group-chat-footer chat-id invitation-admin]]))

(defn render-fn
  [{:keys [type value deleted? deleted-for-me? content-type] :as message-data} _ _ context]
  [rn/view {:style (when platform/android? {:scaleY -1})}
   (if (= type :datemark)
     [quo/divider-date value]
     (if (= content-type constants/content-type-gap)
       [not-implemented/not-implemented
        [message.gap/gap message-data]]
       [rn/view {:padding-horizontal 8}
        (if (or deleted? deleted-for-me?)
          [content.deleted/deleted-message message-data context]
          [message/message-with-reactions message-data context])]))])

(defn messages-list
  [{:keys [chat
           pan-responder
           show-input?
           header-comp]}]
  (let [{:keys [group-chat chat-id public? community-id admins]} chat
        messages                                                 (rf/sub [:chats/raw-chat-messages-stream
                                                                          chat-id])
        bottom-space                                             15]
    [rn/view
     {:style {:flex 1}}
     ;;DO NOT use anonymous functions for handlers
     [rn/flat-list
      (merge
       pan-responder
       {:key-fn                       list-key-fn
        :ref                          list-ref
        :header                       [list-header chat]
        :footer                       [list-footer chat header-comp]
        :data                         messages
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
        :on-scroll-to-index-failed    identity            ;;don't remove this
        :content-container-style      {:padding-top    (+ bottom-space 32)
                                       :padding-bottom 16}
        :scroll-indicator-insets      {:top bottom-space} ;;ios only
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
     #_[quo/floating-shell-button
      (merge {:jump-to
              {:on-press #(do
                            (rf/dispatch [:chat/close true])
                            (rf/dispatch [:shell/navigate-to-jump-to]))
               :label    (i18n/label :t/jump-to)}}
             (when @show-floating-scroll-down-button
               {:scroll-to-bottom {:on-press scroll-to-bottom}}))
      {:position :absolute
       :bottom   6}]]))

(defn scroll-handler
  [event initial-y scroll-y]
  (let [content-size-y (- (oops/oget event "nativeEvent.contentSize.height")
                          (oops/oget event "nativeEvent.layoutMeasurement.height"))
        current-y (+ (oops/oget event "nativeEvent.contentOffset.y") initial-y)]
    (reanimated/set-shared-value scroll-y (- content-size-y current-y))))

(defn messages-list-with-animated-header
  [{:keys [chat show-input? cover-bg-color header-comp footer-comp]}]
  (let [{:keys [group-chat chat-id public? community-id admins]} chat
        messages (rf/sub [:chats/raw-chat-messages-stream chat-id])
        bottom-space 15]
    [safe-area/consumer
     (fn [insets]
       (let [window-height (:height (rn/get-window))
             status-bar-height (rn/status-bar-height)
             bottom-inset (:bottom insets)
             initial-y (if platform/ios? (- (:top insets)) 0)]
         [:f>
          (fn []
            (let [scroll-y (reanimated/use-shared-value initial-y)
                  opacity-animation (reanimated/interpolate scroll-y
                                                            [(* threshold 0.33) (* threshold 0.66)]
                                                            [0 1]
                                                            {:extrapolateLeft  "clamp"
                                                             :extrapolateRight "extend"})
                  translate-animation (reanimated/interpolate scroll-y [(* threshold 0.66) threshold] [50 0]
                                                              {:extrapolateLeft  "clamp"
                                                               :extrapolateRight "clamp"})
                  title-opacity-animation (reanimated/interpolate scroll-y [(* threshold 0.66) threshold] [0 1]
                                                                  {:extrapolateLeft  "clamp"
                                                                   :extrapolateRight "clamp"})]
              [rn/keyboard-avoiding-view
               {:style                  {:position :absolute
                                         :flex     1
                                         :top      0
                                         :left     0
                                         :height   window-height
                                         :bottom   0
                                         :right    0}
                :keyboardVerticalOffset (- bottom-inset)}

               [reanimated/blur-view
                {:blurAmount   32
                 :blurType     :light
                 :overlayColor (if platform/ios? colors/white-opa-70 :transparent)
                 :style        (style/blur-view opacity-animation)}]

               [rn/view {:style {:position       :absolute
                                 :top            56
                                 :left           0
                                 :right          0
                                 :padding-bottom 12
                                 :width          "100%"
                                 :display        :flex
                                 :flex-direction :row
                                 :z-index        2
                                 :overflow       :hidden}}
                [rn/touchable-opacity
                 {:active-opacity 1
                  :on-press       #(rf/dispatch [:navigate-back])
                  :style          (style/button-container {:margin-left 20})}
                 [quo/icon :i/arrow-left {:size 20 :color (colors/theme-colors colors/black colors/white)}]]
                [reanimated/view {:style (style/header-comp translate-animation title-opacity-animation)}
                 [header-comp]]
                [rn/touchable-opacity
                 {:active-opacity 1
                  :style          (style/button-container {:margin-right 20})}
                 [quo/icon :i/options {:size 20 :color (colors/theme-colors colors/black colors/white)}]]]

               [reanimated/flat-list
                {:key-fn                       list-key-fn
                 :ref                          list-ref
                 :header                       [list-header chat]
                 :footer                       (reagent/as-element (list-footer chat (:top insets) scroll-y cover-bg-color))
                 :data                         messages
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
                 :on-scroll-to-index-failed    identity     ;;don't remove this
                 :content-container-style      {:padding-top    (+ bottom-space 32)
                                                :padding-bottom 16}
                 :scroll-indicator-insets      {:top bottom-space} ;;ios only
                 :keyboard-dismiss-mode        :interactive
                 :keyboard-should-persist-taps :handled
                 :onMomentumScrollBegin        state/start-scrolling
                 :onMomentumScrollEnd          state/stop-scrolling
                 :scrollEventThrottle          16
                 :on-scroll                    (fn [event]
                                                 (scroll-handler event initial-y scroll-y)
                                                 (when on-scroll
                                                   (on-scroll event)))
                 ;;TODO https://github.com/facebook/react-native/issues/30034
                 :inverted                     (when platform/ios? true)
                 :style                        (when platform/android? {:scaleY -1})
                 :on-layout                    on-messages-view-layout}]

               #_[reanimated/flat-list
                  {:data                         [nil]
                   :render-fn                    main-comp
                   :key-fn                       str
                   :inverted                     (when platform/ios? true)
                   :style                        (when platform/android? {:scaleY -1})
                   :footer                       (reagent/as-element (header parameters (:top insets) scroll-y))
                   ;; TODO: https://github.com/status-im/status-mobile/issues/14924
                   :scroll-event-throttle        8
                   :keyboard-dismiss-mode        :interactive
                   :keyboard-should-persist-taps :handled
                   :on-scroll                    (fn [event] (scroll-handler event initial-y scroll-y))}]

               (when footer-comp
                 (footer-comp insets))]))]))]))
