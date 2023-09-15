(ns status-im2.contexts.chat.messages.list.view
  (:require
    [oops.core :as oops]
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.background-timer :as background-timer]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [status-im.ui.screens.chat.group :as chat.group]
    [status-im.ui.screens.chat.message.gap :as message.gap]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.messages.content.view :as message]
    [status-im2.contexts.chat.messages.list.state :as state]
    [status-im2.contexts.chat.messages.list.style :as style]
    [status-im2.contexts.chat.composer.constants :as composer.constants]
    [status-im2.contexts.chat.messages.navigation.style :as navigation.style]
    [status-im2.contexts.shell.jump-to.constants :as jump-to.constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defonce ^:const threshold-percentage-to-show-floating-scroll-down-button 75)
(defonce ^:const loading-indicator-extra-spacing 250)
(defonce ^:const loading-indicator-page-loading-height 100)
(defonce ^:const scroll-animation-input-range [50 125])
(defonce ^:const min-message-height 32)

(defonce extra-keyboard-height (reagent/atom 0))
(defonce messages-list-ref (atom nil))
(defonce messages-view-height (reagent/atom 0))
(defonce messages-view-header-height (reagent/atom 0))
(defonce show-floating-scroll-down-button? (reagent/atom false))

(defn list-key-fn [{:keys [message-id value]}] (or message-id value))
(defn list-ref [ref] (reset! messages-list-ref ref))

(defn scroll-to-bottom
  []
  (some-> ^js @messages-list-ref
          (.scrollToOffset #js
                            {:animated true})))

(defn on-scroll
  [evt]
  (let [y                  (oops/oget evt "nativeEvent.contentOffset.y")
        layout-height      (oops/oget evt "nativeEvent.layoutMeasurement.height")
        threshold-height   (* (/ layout-height 100)
                              threshold-percentage-to-show-floating-scroll-down-button)
        reached-threshold? (> y threshold-height)]
    (when (not= reached-threshold? @show-floating-scroll-down-button?)
      (rn/configure-next (:ease-in-ease-out rn/layout-animation-presets))
      (reset! show-floating-scroll-down-button? reached-threshold?))))

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
  [scroll-y]
  ;; FIXME: that's a bit of a hack but we need to update `scroll-y` once the new messages
  ;; are fetched in order for the header to work properly
  (let [on-loaded (fn [n]
                    (reanimated/set-shared-value scroll-y
                                                 (+ (reanimated/get-shared-value scroll-y)
                                                    (* n 200))))]
    (if @state/scrolling
      (rf/dispatch [:chat.ui/load-more-messages-for-current-chat on-loaded])
      (background-timer/set-timeout #(rf/dispatch [:chat.ui/load-more-messages-for-current-chat
                                                   on-loaded])
                                    (if platform/low-device? 700 100)))))

(defn contact-icon
  [{:keys [ens-verified added?]}]
  (when (or ens-verified added?)
    [rn/view
     {:style {:padding-left 10
              :margin-top   2}}
     (if ens-verified
       [quo/icon :i/verified
        {:no-color true
         :size     20
         :color    (colors/theme-colors colors/success-50 colors/success-60)}]
       (when added?
         [quo/icon :i/contact
          {:no-color true
           :size     20
           :color    (colors/theme-colors colors/primary-50 colors/primary-60)}]))]))

(def header-extrapolation-option
  {:extrapolateLeft  "clamp"
   :extrapolateRight "clamp"})

(defn skeleton-list-props
  [content parent-height animated?]
  {:content       content
   :parent-height parent-height
   :animated?     animated?})

(defn loading-view
  [chat-id]
  (let [loading-messages?   (rf/sub [:chats/loading-messages? chat-id])
        all-loaded?         (rf/sub [:chats/all-loaded? chat-id])
        messages            (rf/sub [:chats/raw-chat-messages-stream chat-id])
        loading-first-page? (= (count messages) 0)
        top-spacing         (if loading-first-page? 0 navigation.style/navigation-bar-height)
        parent-height       (if loading-first-page?
                              (- @messages-view-height
                                 @messages-view-header-height
                                 composer.constants/composer-default-height
                                 loading-indicator-extra-spacing)
                              loading-indicator-page-loading-height)]
    (when (or loading-messages? (not all-loaded?))
      [rn/view {:padding-top top-spacing}
       [quo/skeleton-list (skeleton-list-props :messages parent-height true)]])))

(defn list-header
  [insets able-to-send-message?]
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :height           (+ (if able-to-send-message?
                           (+ composer.constants/composer-default-height
                              jump-to.constants/floating-shell-button-height
                              (:bottom insets))
                           (- 70 (:bottom insets))))}])

(defn f-list-footer-avatar
  [{:keys [scroll-y display-name online? profile-picture]}]
  (let [image-scale-animation       (reanimated/interpolate scroll-y
                                                            scroll-animation-input-range
                                                            [1 0.5]
                                                            header-extrapolation-option)
        image-top-margin-animation  (reanimated/interpolate scroll-y
                                                            scroll-animation-input-range
                                                            [0 40]
                                                            header-extrapolation-option)
        image-side-margin-animation (reanimated/interpolate scroll-y
                                                            scroll-animation-input-range
                                                            [0 -20]
                                                            header-extrapolation-option)]
    [reanimated/view
     {:style (style/header-image image-scale-animation
                                 image-top-margin-animation
                                 image-side-margin-animation)}
     [quo/user-avatar
      {:full-name       display-name
       :online?         online?
       :profile-picture profile-picture
       :size            :big}]]))

(defn list-footer-avatar
  [props]
  [:f> f-list-footer-avatar props])

;;TODO(rasom) https://github.com/facebook/react-native/issues/30034
(defn- add-inverted-y-android
  [style]
  (cond-> style
    platform/android?
    (assoc :scale-y -1)))

(defn actions
  [chat-id cover-bg-color]
  (let [latest-pin-text (rf/sub [:chats/last-pinned-message-text chat-id])
        pins-count      (rf/sub [:chats/pin-messages-count chat-id])]
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
                                                      chat-id]))}]}]))

(defn f-list-footer
  [{:keys [chat scroll-y cover-bg-color on-layout]}]
  (let [{:keys [chat-id chat-name emoji chat-type
                group-chat]} chat
        all-loaded?          (rf/sub [:chats/all-loaded? chat-id])
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
        border-animation     (reanimated/interpolate scroll-y
                                                     [30 125]
                                                     [14 0]
                                                     header-extrapolation-option)]
    [rn/view (add-inverted-y-android {:flex 1})
     [rn/view
      {:style     (style/header-container all-loaded?)
       :on-layout on-layout}
      [rn/view {:style (style/header-cover cover-bg-color)}]
      [reanimated/view {:style (style/header-bottom-part border-animation)}
       [rn/view {:style style/header-avatar}
        [rn/view {:style {:align-items :flex-start}}
         (when-not group-chat
           [list-footer-avatar
            {:scroll-y        scroll-y
             :display-name    display-name
             :online?         online?
             :profile-picture photo-path}])]
        [quo/text
         {:weight          :semi-bold
          :size            :heading-1
          :style           {:margin-top (if group-chat 54 12)}
          :number-of-lines 1}
         display-name
         [contact-icon contact]]
        (when bio
          [quo/text {:style style/bio}
           bio])
        [actions chat-id cover-bg-color]]]]
     [loading-view chat-id]]))

(defn list-footer
  [props]
  [:f> f-list-footer props])

(defn list-group-chat-header
  [{:keys [chat-id invitation-admin]}]
  [rn/view
   [chat.group/group-chat-footer chat-id invitation-admin]])
(defn footer-on-layout
  [e]
  (let [height (oops/oget e "nativeEvent.layout.height")
        y      (oops/oget e "nativeEvent.layout.y")]
    (reset! messages-view-header-height (+ height y))))

(defn render-fn
  [{:keys [type value content-type] :as message-data} _ _
   {:keys [context keyboard-shown?]}]
  (when (not= content-type constants/content-type-contact-request)
    [rn/view
     (add-inverted-y-android {:background-color (colors/theme-colors colors/white colors/neutral-95)})
     (cond
       (= type :datemark)
       [quo/divider-date value]

       (= content-type constants/content-type-gap)
       [message.gap/gap message-data]

       :else
       [message/message message-data context keyboard-shown?])]))

(defn scroll-handler
  [event scroll-y]
  (let [content-size-y (- (oops/oget event "nativeEvent.contentSize.height")
                          (oops/oget event "nativeEvent.layoutMeasurement.height"))
        current-y      (oops/oget event "nativeEvent.contentOffset.y")]
    (reanimated/set-shared-value scroll-y (- content-size-y current-y))))

(defn f-messages-list-content
  [{:keys [chat insets scroll-y content-height cover-bg-color keyboard-shown?]}]
  (let [{window-height :height}   (rn/get-window)
        {:keys [keyboard-height]} (hooks/use-keyboard)
        context                   (rf/sub [:chats/current-chat-message-list-view-context])
        messages                  (rf/sub [:chats/raw-chat-messages-stream (:chat-id chat)])
        recording?                (rf/sub [:chats/recording?])
        all-loaded?               (rf/sub [:chats/all-loaded? (:chat-id chat)])]
    [rn/view {:style {:flex 1}}
     [rn/flat-list
      {:key-fn                            list-key-fn
       :ref                               list-ref
       :header                            [:<>
                                           [list-header insets (:able-to-send-message? context)]
                                           (when (= (:chat-type chat) constants/private-group-chat-type)
                                             [list-group-chat-header chat])]
       :footer                            [list-footer
                                           {:chat           chat
                                            :scroll-y       scroll-y
                                            :cover-bg-color cover-bg-color
                                            :on-layout      footer-on-layout}]
       :data                              messages
       :render-data                       {:context         context
                                           :keyboard-shown? keyboard-shown?
                                           :insets          insets}
       :render-fn                         render-fn
       :on-viewable-items-changed         on-viewable-items-changed
       :on-content-size-change            (fn [_ y]
                                            ;; NOTE(alwx): here we set the initial value of `scroll-y`
                                            ;; which is needed because by default the chat is
                                            ;; scrolled to the bottom and no initial `on-scroll`
                                            ;; event is getting triggered
                                            (let [scroll-y-shared       (reanimated/get-shared-value
                                                                         scroll-y)
                                                  content-height-shared (reanimated/get-shared-value
                                                                         content-height)]
                                              (when (or (= scroll-y-shared 0)
                                                        (> (Math/abs (- content-height-shared y))
                                                           min-message-height))
                                                (reanimated/set-shared-value scroll-y
                                                                             (- y
                                                                                window-height
                                                                                (- (when keyboard-shown?
                                                                                     keyboard-height))))
                                                (reanimated/set-shared-value content-height y))))
       :on-end-reached                    #(list-on-end-reached scroll-y)
       :on-scroll-to-index-failed         identity
       :scroll-indicator-insets           {:top (if (:able-to-send-message? context)
                                                  (- composer.constants/composer-default-height 16)
                                                  0)}
       :keyboard-dismiss-mode             :interactive
       :keyboard-should-persist-taps      :always
       :on-scroll-begin-drag              rn/dismiss-keyboard!
       :on-momentum-scroll-begin          state/start-scrolling
       :on-momentum-scroll-end            state/stop-scrolling
       :scroll-event-throttle             16
       :on-scroll                         (fn [event]
                                            (scroll-handler event scroll-y)
                                            (when on-scroll
                                              (on-scroll event)))
       :style                             (add-inverted-y-android
                                           {:background-color (if all-loaded?
                                                                (colors/theme-colors
                                                                 (colors/custom-color cover-bg-color
                                                                                      50
                                                                                      20)
                                                                 (colors/custom-color cover-bg-color
                                                                                      50
                                                                                      40))
                                                                (colors/theme-colors
                                                                 colors/white
                                                                 colors/neutral-95))})
       ;;TODO(rasom) https://github.com/facebook/react-native/issues/30034
       :inverted                          (when platform/ios? true)
       :on-layout                         (fn [e]
                                            (let [layout-height (oops/oget e
                                                                           "nativeEvent.layout.height")]
                                              (reset! messages-view-height layout-height)))
       :scroll-enabled                    (not recording?)
       :content-inset-adjustment-behavior :never}]]))

(defn message-list-content-view
  [props]
  (let [chat-screen-loaded? (rf/sub [:shell/chat-screen-loaded?])
        window-height       (:height (rn/get-window))
        content-height      (- window-height composer.constants/composer-default-height)
        top-spacing         (when (not chat-screen-loaded?) navigation.style/navigation-bar-height)]
    (if chat-screen-loaded?
      [:f> f-messages-list-content props]
      [rn/view {:style {:padding-top top-spacing :flex 1}}
       [quo/skeleton-list (skeleton-list-props :messages content-height false)]])))
