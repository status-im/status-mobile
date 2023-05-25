(ns status-im2.contexts.chat.messages.list.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [react-native.background-timer :as background-timer]
            [react-native.core :as rn]
            [react-native.hooks :as hooks]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [status-im.ui.screens.chat.group :as chat.group]
            [status-im.ui.screens.chat.message.gap :as message.gap]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.messages.content.deleted.view :as content.deleted]
            [status-im2.contexts.chat.messages.content.view :as message]
            [status-im2.contexts.chat.messages.list.state :as state]
            [status-im2.contexts.chat.messages.list.style :as style]
            [status-im2.contexts.chat.messages.navigation.style :as navigation.style]
            [status-im2.contexts.chat.composer.constants :as composer.constants]
            [utils.re-frame :as rf]))

(defonce ^:const threshold-percentage-to-show-floating-scroll-down-button 75)
(defonce ^:const loading-indicator-extra-spacing 250)
(defonce ^:const loading-indicator-page-loading-height 100)
(defonce ^:const scroll-animation-input-range [50 125])
(defonce ^:const spacing-between-composer-and-content 64)

(defonce messages-list-ref (atom nil))
(defonce messages-view-height (reagent/atom 0))
(defonce messages-view-header-height (reagent/atom 0))
(defonce show-floating-scroll-down-button (reagent/atom false))

(defn list-key-fn [{:keys [message-id value]}] (or message-id value))
(defn list-ref [ref] (reset! messages-list-ref ref))

(defn scroll-to-offset
  [position]
  (some-> ^js @messages-list-ref
          (.scrollToOffset #js
                            {:offset   position
                             :animated true})))

(defn scroll-to-bottom
  []
  (scroll-to-offset (- 0 style/messages-list-bottom-offset)))

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
  [e]
  (when @messages-list-ref
    (reset! state/first-not-visible-item
      (when-let [last-visible-element (aget (oops/oget e "viewableItems")
                                            (dec (oops/oget e "viewableItems.length")))]
        (let [index             (oops/oget last-visible-element "index")
              ;; Get first not visible element, if it's a datemark/gap
              ;; we might unnecessarely add messages on receiving as
              ;; they do not have a clock value, but most of the times
              ;; it will be a message
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
                                    (if platform/low-device? 7000 5000)))))

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

(defn loading-view
  [chat-id insets]
  (let [loading-messages?   (rf/sub [:chats/loading-messages? chat-id])
        all-loaded?         (rf/sub [:chats/all-loaded? chat-id])
        messages            (rf/sub [:chats/raw-chat-messages-stream chat-id])
        loading-first-page? (= (count messages) 0)
        top-spacing         (if loading-first-page? 0 navigation.style/navigation-bar-height)]
    (when (or loading-messages? (not all-loaded?))
      [rn/view {:padding-top top-spacing}
       [quo/skeleton
        (if loading-first-page?
          (- @messages-view-height
             @messages-view-header-height
             composer.constants/composer-default-height
             loading-indicator-extra-spacing)
          loading-indicator-page-loading-height)]])))

(defn list-header
  [insets]
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :margin-bottom    (- 0
                         (:top insets)
                         (when platform/ios? style/overscroll-cover-height))
    :height           (+ composer.constants/composer-default-height
                         (:bottom insets)
                         spacing-between-composer-and-content
                         (when platform/ios? style/overscroll-cover-height))}])

(defn f-list-footer
  [{:keys [chat insets scroll-y cover-bg-color on-layout]}]
  (let [{:keys [chat-id chat-name emoji chat-type
                group-chat]}        chat
        all-loaded?                 (rf/sub [:chats/all-loaded? chat-id])
        display-name                (if (= chat-type constants/one-to-one-chat-type)
                                      (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
                                      (str emoji " " chat-name))
        {:keys [bio]}               (rf/sub [:contacts/contact-by-identity chat-id])
        online?                     (rf/sub [:visibility-status-updates/online? chat-id])
        contact                     (when-not group-chat
                                      (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path                  (when-not (empty? (:images contact))
                                      (rf/sub [:chats/photo-path chat-id]))
        border-animation            (reanimated/interpolate scroll-y
                                                            [30 125]
                                                            [14 0]
                                                            header-extrapolation-option)
        image-scale-animation       (reanimated/interpolate scroll-y
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
    [rn/view {:flex 1}
     [rn/view
      {:style     (style/header-container all-loaded?)
       :on-layout on-layout}
      (when cover-bg-color
        [rn/view {:style (style/header-cover cover-bg-color insets)}])
      [reanimated/view {:style (style/header-bottom-part border-animation)}
       [rn/view {:style style/header-avatar}
        (when-not group-chat
          [rn/view {:style {:align-items :flex-start}}
           [reanimated/view
            {:style (style/header-image image-scale-animation
                                        image-top-margin-animation
                                        image-side-margin-animation)}
            [quo/user-avatar
             {:full-name       display-name
              :online?         online?
              :profile-picture photo-path
              :size            :big}]]])
        [rn/view {:style style/name-container}
         [quo/text
          {:weight          :semi-bold
           :size            :heading-1
           :style           {:margin-top (if group-chat 54 12)}
           :number-of-lines 1}
          display-name
          [contact-icon contact]]]
        (when bio
          [quo/text {:style style/bio}
           bio])]]]
     [loading-view chat-id insets]]))

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
  [{:keys [type value deleted? deleted-for-me? content-type] :as message-data} _ _
   {:keys [context keyboard-shown?]}]
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-95)}
   (if (= type :datemark)
     [quo/divider-date value]
     (if (= content-type constants/content-type-gap)
       [not-implemented/not-implemented
        [message.gap/gap message-data]]
       [rn/view {:padding-horizontal 8}
        (if (or deleted? deleted-for-me?)
          [content.deleted/deleted-message message-data context]
          [message/message-with-reactions message-data context keyboard-shown?])]))])

(defn scroll-handler
  [event scroll-y]
  (let [content-size-y (- (oops/oget event "nativeEvent.contentSize.height")
                          (oops/oget event "nativeEvent.layoutMeasurement.height"))
        current-y      (oops/oget event "nativeEvent.contentOffset.y")]
    (reanimated/set-shared-value scroll-y (- content-size-y current-y))))

(defn messages-list-content
  [{:keys [chat insets scroll-y cover-bg-color keyboard-shown?]}]
  (let [context     (rf/sub [:chats/current-chat-message-list-view-context])
        messages    (rf/sub [:chats/raw-chat-messages-stream (:chat-id chat)])
        recording?  (rf/sub [:chats/recording?])
        all-loaded? (rf/sub [:chats/all-loaded? (:chat-id chat)])]
    [rn/view {:style {:flex 1}}
     [rn/flat-list
      {:key-fn                       list-key-fn
       :ref                          list-ref
       :header                       [:<>
                                      (when (= (:chat-type chat) constants/private-group-chat-type)
                                        [list-group-chat-header chat])
                                      [list-header insets]]
       :footer                       [list-footer
                                      {:chat           chat
                                       :insets         insets
                                       :scroll-y       scroll-y
                                       :cover-bg-color cover-bg-color
                                       :on-layout      footer-on-layout}]
       :data                         messages
       :render-data                  {:context         context
                                      :keyboard-shown? keyboard-shown?}
       :render-fn                    render-fn
       :on-viewable-items-changed    on-viewable-items-changed
       :on-end-reached               #(list-on-end-reached scroll-y)
       :on-scroll-to-index-failed    identity
       :content-container-style      {:padding-bottom style/messages-list-bottom-offset}
       :scroll-indicator-insets      {:top (+ composer.constants/composer-default-height
                                              (:bottom insets))}
       :keyboard-dismiss-mode        :interactive
       :keyboard-should-persist-taps :handled
       :on-momentum-scroll-begin     state/start-scrolling
       :on-momentum-scroll-end       state/stop-scrolling
       :scroll-event-throttle        16
       :on-scroll                    (fn [event]
                                       (scroll-handler event scroll-y)
                                       (when on-scroll
                                         (on-scroll event)))
       :style                        {:background-color (if all-loaded?
                                                          cover-bg-color
                                                          (colors/theme-colors colors/white
                                                                               colors/neutral-95))}
       :inverted                     true
       :on-layout                    (fn [e]
                                       (when platform/android?
                                         ;; FIXME: this is due to Android not triggering the initial
                                         ;; scrollTo event
                                         (scroll-to-offset 1))
                                       (let [layout-height (oops/oget e "nativeEvent.layout.height")]
                                         (reset! messages-view-height layout-height)))
       :scroll-enabled               (not recording?)}]]))

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

(defn f-messages-list
  [{:keys [chat cover-bg-color header-comp footer-comp]}]
  (let [insets                    (safe-area/get-insets)
        scroll-y                  (reanimated/use-shared-value 0)
        {:keys [keyboard-height]} (hooks/use-keyboard)
        {keyboard-shown? :shown?} (use-keyboard-visibility)]
    (rn/use-effect
     (fn []
       (when keyboard-shown?
         (reanimated/set-shared-value scroll-y
                                      (+ (reanimated/get-shared-value scroll-y)
                                         keyboard-height))))
     [keyboard-shown? keyboard-height])
    [rn/keyboard-avoiding-view
     {:style                    (style/keyboard-avoiding-container insets)
      :keyboard-vertical-offset (- (:bottom insets))}

     (when header-comp
       [header-comp {:scroll-y scroll-y}])

     [messages-list-content
      {:chat            chat
       :insets          insets
       :scroll-y        scroll-y
       :cover-bg-color  cover-bg-color
       :keyboard-shown? keyboard-shown?}]

     (when footer-comp
       (footer-comp {:insets insets}))]))

(defn messages-list
  [props]
  [:f> f-messages-list props])
