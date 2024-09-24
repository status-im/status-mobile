(ns status-im.contexts.chat.messenger.messages.list.view
  (:require
    [clojure.string :as string]
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

(defn- contact-icon
  [{:keys [ens-verified added?]} theme]
  (when (or ens-verified added?)
    [rn/view
     {:style {:margin-left 4}}
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
     ;; Don't use animated loading skeleton https://github.com/status-im/status-mobile/issues/17426
     [quo/skeleton-list (skeleton-list-props :messages parent-height false)]]))

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

(defn list-header
  [insets able-to-send-message?]
  (let [header-data {:insets                insets
                     :able-to-send-message? able-to-send-message?
                     :input-content-height  (:input-content-height (rf/sub [:chats/current-chat-input]))
                     :images                (rf/sub [:chats/sending-image])
                     :reply                 (rf/sub [:chats/reply-message])
                     :edit                  (rf/sub [:chats/edit-message])
                     :link-previews?        (or (rf/sub [:chats/link-previews?])
                                                (rf/sub [:chats/status-link-previews?]))}]
    [rn/view {:style {:height (header-height header-data)}}]))

(defn list-footer-avatar
  [{:keys [distance-from-list-top display-name online? profile-picture theme group-chat color
           emoji chat-type chat-name empty-chat?]}]
  (let [scale              (reanimated/interpolate
                            distance-from-list-top
                            [0 (if empty-chat? 0 messages.constants/header-container-top-margin)]
                            [1 0.4]
                            messages.constants/default-extrapolation-option)
        top                (reanimated/interpolate
                            distance-from-list-top
                            [0 (if empty-chat? 0 messages.constants/header-container-top-margin)]
                            [-44 -12]
                            messages.constants/default-extrapolation-option)
        left               (reanimated/interpolate
                            distance-from-list-top
                            [0 (if empty-chat? 0 messages.constants/header-container-top-margin)]
                            [16 -8]
                            messages.constants/default-extrapolation-option)
        community-channel? (= chat-type constants/community-chat-type)]
    [reanimated/view
     {:style (style/header-image scale top left theme)}
     (cond
       community-channel?
       [quo/channel-avatar
        {:size                :size-80
         :full-name           chat-name
         :customization-color color
         :emoji               (when-not (string/blank? emoji)
                                (string/trim emoji))}]
       group-chat
       [quo/group-avatar
        {:customization-color color
         :size                :size-80
         :picture             profile-picture
         :chat-name           chat-name}]
       :else
       [quo/user-avatar
        {:full-name       display-name
         :online?         online?
         :profile-picture profile-picture
         :size            :big}])]))

(defn chat-display-name
  [{:keys [distance-from-list-top display-name contact theme empty-chat?]}]
  (let [top  (reanimated/interpolate
              distance-from-list-top
              [0 (if empty-chat? 0 messages.constants/header-container-top-margin)]
              [0 -35]
              messages.constants/default-extrapolation-option)
        left (reanimated/interpolate
              distance-from-list-top
              [0 (if empty-chat? 0 messages.constants/header-container-top-margin)]
              [0 40]
              messages.constants/default-extrapolation-option)]
    [reanimated/view
     {:style (style/user-name-container top left)}
     [rn/view
      {:style style/user-name}
      [quo/text
       {:weight          :semi-bold
        :size            :heading-1
        :style           {:flex-shrink 1}
        :number-of-lines 1}
       display-name]
      [contact-icon contact theme]]]))

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

(defn bio-and-actions
  [{:keys [distance-from-list-top bio chat-id customization-color empty-chat? description]}]
  (let [has-bio (seq (or bio description))
        top     (reanimated/interpolate
                 distance-from-list-top
                 [0 (if empty-chat? 0 messages.constants/header-container-top-margin)]
                 [(if has-bio 8 16) (if has-bio -28 -20)]
                 messages.constants/default-extrapolation-option)]
    [reanimated/view
     {:style (style/bio-and-actions top)}
     (when has-bio
       [quo/text (or bio description)])
     [actions chat-id customization-color]]))

(defn footer-component
  [{:keys [chat distance-from-list-top customization-color]}]
  (let [theme                 (quo.theme/use-theme)
        {:keys [chat-id chat-name emoji chat-type
                group-chat color description
                empty-chat?]} chat
        display-name          (cond
                                (= chat-type constants/one-to-one-chat-type)
                                (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
                                (= chat-type constants/community-chat-type)
                                (str "# " chat-name)
                                :else (str emoji chat-name))
        {:keys [bio]}         (rf/sub [:contacts/contact-by-identity chat-id])
        online?               (rf/sub [:visibility-status-updates/online? chat-id])
        contact               (when-not group-chat
                                (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path            (if group-chat
                                (rf/sub [:chats/group-chat-image chat-id])
                                (rf/sub [:chats/photo-path chat-id]))
        top-margin            (+ (safe-area/get-top)
                                 messages.constants/top-bar-height
                                 messages.constants/header-container-top-margin
                                 32)
        background-color      (colors/theme-colors
                               (colors/resolve-color customization-color theme 20)
                               (colors/resolve-color customization-color theme 40)
                               theme)
        bottom                (reanimated/interpolate
                               distance-from-list-top
                               [0 messages.constants/header-container-top-margin]
                               [32 -4]
                               messages.constants/default-extrapolation-option)
        background-opacity    (reanimated/interpolate
                               distance-from-list-top
                               [messages.constants/header-container-top-margin
                                (+ messages.constants/header-animation-distance
                                   messages.constants/header-container-top-margin)]
                               [1 0]
                               messages.constants/default-extrapolation-option)]
    [:<>
     [reanimated/view
      {:style (style/background-container background-color background-opacity top-margin)}]
     [reanimated/view {:style (style/header-bottom-container bottom top-margin)}
      [rn/view {:style (style/header-bottom-shadow theme)}]
      [rn/view {:style (style/header-bottom-part theme)}
       [list-footer-avatar
        {:distance-from-list-top distance-from-list-top
         :display-name           display-name
         :online?                online?
         :theme                  theme
         :profile-picture        photo-path
         :group-chat             group-chat
         :color                  color
         :emoji                  emoji
         :chat-type              chat-type
         :chat-name              chat-name
         :empty-chat?            empty-chat?}]
       [chat-display-name
        {:distance-from-list-top distance-from-list-top
         :display-name           display-name
         :theme                  theme
         :contact                contact
         :group-chat             group-chat
         :empty-chat?            empty-chat?}]
       [bio-and-actions
        {:distance-from-list-top distance-from-list-top
         :bio                    bio
         :chat-id                chat-id
         :customization-color    customization-color
         :description            description
         :empty-chat?            empty-chat?}]]]]))

(defn list-footer
  [props]
  (let [chat-id           (get-in props [:chat :chat-id])
        loading-messages? (rf/sub [:chats/loading-messages? chat-id])
        all-loaded?       (rf/sub [:chats/all-loaded? chat-id])]
    [:<>
     (if (or loading-messages? (not all-loaded?))
       [loading-view chat-id props]
       [footer-component props])]))

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
  [{:keys [event layout-height distance-atom distance-from-list-top
           chat-screen-layout-calculations-complete?]}]
  (let [layout-height-new (oops/oget event "nativeEvent.layout.height")
        change            (- layout-height-new @layout-height)
        new-distance      (- @distance-atom change)]
    (when (and (not= change 0) (not= @layout-height layout-height-new))
      (reanimated/set-shared-value distance-from-list-top new-distance)
      (reset! distance-atom new-distance)
      (reset! layout-height layout-height-new))
    (when-not (reanimated/get-shared-value chat-screen-layout-calculations-complete?)
      (reanimated/set-shared-value chat-screen-layout-calculations-complete? true))))

(defn on-scroll-fn
  [distance-atom layout-height-atom]
  (fn [layout-height new-distance]
    (when (not= layout-height @layout-height-atom)
      (reset! layout-height-atom layout-height))
    (reset! distance-atom new-distance)))

(defn messages-list-content
  [{:keys [insets distance-from-list-top content-height layout-height distance-atom
           chat-screen-layout-calculations-complete? chat-list-scroll-y]}]
  (let [theme                                (quo.theme/use-theme)
        {:keys [chat-type chat-id] :as chat} (rf/sub [:chats/current-chat-chat-view])
        one-to-one-chat?                     (= chat-type constants/one-to-one-chat-type)
        community-channel?                   (= constants/community-chat-type chat-type)
        {contact-customization-color
         :customization-color}               (when one-to-one-chat?
                                               (rf/sub [:contacts/contact-by-identity chat-id]))
        customization-color                  (cond community-channel?
                                                   (or (:color chat)
                                                       (rf/sub [:communities/community-color
                                                                (:community-id chat)]))
                                                   one-to-one-chat? contact-customization-color
                                                   :else (or (:color chat) :turquoise))
        {:keys [keyboard-shown]}             (hooks/use-keyboard)
        {window-height :height}              (rn/get-window)
        context                              (rf/sub [:chats/current-chat-message-list-view-context])
        able-to-send-message?                (:able-to-send-message? context)
        messages                             (rf/sub [:chats/raw-chat-messages-stream chat-id])
        margin-bottom?                       (and community-channel? (not able-to-send-message?))
        recording?                           (rf/sub [:chats/recording?])]
    [rn/view {:style (style/permission-context-sheet margin-bottom?)}
     [rn/view {:style {:flex-shrink 1}} ;; Keeps flat list on top
      [reanimated/flat-list
       {:key-fn                            list-key-fn
        :ref                               list-ref
        :bounces                           false
        :header                            (when (= (:chat-type chat) constants/private-group-chat-type)
                                             [list-group-chat-header chat])
        :footer                            [list-footer
                                            {:theme                  theme
                                             :chat                   chat
                                             :window-height          window-height
                                             :distance-from-list-top distance-from-list-top
                                             :customization-color    customization-color}]
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
        :content-container-style           {:padding-top distance-from-last-message}
        :inverted                          true
        :on-layout                         #(on-layout
                                             {:event %
                                              :layout-height layout-height
                                              :distance-atom distance-atom
                                              :distance-from-list-top distance-from-list-top
                                              :chat-screen-layout-calculations-complete?
                                              chat-screen-layout-calculations-complete?})
        :scroll-enabled                    (not recording?)
        :content-inset-adjustment-behavior :never
        :scroll-indicator-insets           {:right 1}}]]]))
