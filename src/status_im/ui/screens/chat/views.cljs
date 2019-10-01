(ns status-im.ui.screens.chat.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.contact.db :as contact.db]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.chat.actions :as actions]
            [status-im.ui.screens.chat.input.input :as input]
            [status-im.ui.screens.chat.message.datemark :as message-datemark]
            [status-im.ui.screens.chat.message.gap :as gap]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.screens.chat.message.options :as message-options]
            [status-im.ui.screens.chat.stickers.views :as stickers]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.ui.screens.chat.toolbar-content :as toolbar-content]
            [status-im.ui.screens.profile.tribute-to-talk.views
             :as
             tribute-to-talk.views]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn add-contact-bar
  [public-key]
  [react/touchable-highlight
   {:on-press
    #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])
    :accessibility-label :add-to-contacts-button
    :style style/add-contact}
   [react/view style/add-contact-center
    [vector-icons/icon :main-icons/add
     {:color colors/blue}]
    [react/i18n-text {:style style/add-contact-text :key :add-to-contacts}]]])

(defn- on-options
  [chat-id chat-name group-chat? public?]
  (list-selection/show {:title   chat-name
                        :options (actions/actions group-chat? chat-id public?)}))

(defmulti message-row
  (fn [{{:keys [type]} :row}] type))

(defmethod message-row :datemark
  [{{:keys [value]} :row}]
  [message-datemark/chat-datemark-mobile value])

(defmethod message-row :gap
  [{:keys [row idx list-ref]}]
  [gap/gap row idx list-ref])

(defmethod message-row :default
  [{:keys [group-chat current-public-key modal? row]}]
  [message/chat-message (assoc row
                               :group-chat group-chat
                               :modal? modal?
                               :current-public-key current-public-key)])

(def animation-duration 200)

(defview messages-view-animation [add-contact-bar message-view]
  ;; smooths out appearance of message-view
  (letsubs [opacity (animation/create-value 0)]
    {:component-did-mount (fn [_]
                            (animation/start
                             (animation/timing
                              opacity
                              {:toValue         1
                               :duration        animation-duration
                               :useNativeDriver true})))}
    (if platform/desktop?
      message-view
      [react/animated-view {:style (style/message-view-animated opacity)}
       add-contact-bar
       message-view])))

(defn tribute-to-talk-header
  [name]
  [react/nested-text {:style (assoc style/intro-header-description
                                    :margin-bottom 32)}
   (i18n/label :t/tribute-required-by-multiaccount {:multiaccount-name name})
   [{:style {:color colors/blue}
     :on-press #(re-frame/dispatch [:navigate-to :tribute-learn-more])}
    (str " " (i18n/label :learn-more))]])

(defn intro-header
  [contact]
  [react/text {:style (assoc style/intro-header-description
                             :margin-bottom 32)}
   (str (i18n/label :t/empty-chat-description-one-to-one) (multiaccounts/displayed-name contact))])

(defn join-chat-button [chat-id]
  [button/button
   {:type     :secondary
    :on-press #(re-frame/dispatch [:group-chats.ui/join-pressed chat-id])
    :label    :t/join-group-chat}])

(defn decline-chat [chat-id]
  [react/touchable-highlight
   {:on-press
    #(re-frame/dispatch [:group-chats.ui/remove-chat-confirmed chat-id])}
   [react/text {:style style/decline-chat}
    (i18n/label :t/group-chat-decline-invitation)]])

(defn group-chat-footer
  [chat-id]
  [react/view {:style style/group-chat-join-footer}
   [react/view {:style style/group-chat-join-container}
    [join-chat-button chat-id]
    [decline-chat chat-id]]])

;; TODO this is now used only in Desktop - unnecessary for mobile
(defn group-chat-join-section
  [inviter-name {:keys [name group-chat color chat-id]}]
  [react/view style/empty-chat-container
   [react/view {:style {:margin-bottom 170}}
    [chat-icon.screen/profile-icon-view
     nil name color false 100
     {:default-chat-icon-text style/group-chat-icon}]]
   [react/view {:style style/group-chat-join-footer}
    [react/view {:style style/group-chat-join-container}
     [react/view
      [react/text {:style style/group-chat-join-name} name]]
     [react/text {:style style/intro-header-description}
      (i18n/label :t/join-group-chat-description {:username inviter-name
                                                  :group-name name})]
     [join-chat-button chat-id]
     [decline-chat chat-id]]]])

(defn group-chat-description-loading
  []
  [react/view {:style (merge style/intro-header-description-container
                             {:margin-bottom 36
                              :height        44})}
   [react/text {:style style/intro-header-description}
    (i18n/label :t/loading)]
   [react/activity-indicator {:animating true
                              :size      :small
                              :color     colors/gray}]])

(defn group-chat-description-container
  [{:keys [group-chat name pending-invite-inviter-name
           inviter-name color chat-id chat-name public?
           contact universal-link range intro-status] :as chat}]
  (let [{:keys [lowest-request-from highest-request-to]} range]
    (case intro-status
      :loading
      [group-chat-description-loading]

      :empty
      (when public?
        [react/nested-text {:style (merge style/intro-header-description
                                          {:margin-bottom 36})}
         (let [quiet-hours (quot (- highest-request-to lowest-request-from)
                                 (* 60 60))
               quiet-time  (if (<= quiet-hours 24)
                             (i18n/label :t/quiet-hours
                                         {:quiet-hours quiet-hours})
                             (i18n/label :t/quiet-days
                                         {:quiet-days (quot quiet-hours 24)}))]
           (i18n/label :t/empty-chat-description-public
                       {:quiet-hours quiet-time}))
         [{:style    {:color colors/blue}
           :on-press #(list-selection/open-share
                       {:message
                        (i18n/label
                         :t/share-public-chat-text {:link universal-link})})}
          (i18n/label :t/empty-chat-description-public-share-this)]])

      :messages
      (when (not public?)
        (if pending-invite-inviter-name
          [react/nested-text {:style style/intro-header-description}
           [{:style {:color :black}} pending-invite-inviter-name]
           (i18n/label :t/join-group-chat-description
                       {:username   ""
                        :group-name chat-name})]
          (if (not= inviter-name "Unknown")
            [react/nested-text {:style style/intro-header-description}
             (i18n/label :t/joined-group-chat-description
                         {:username   ""
                          :group-name chat-name})
             [{:style {:color :black}} inviter-name]]
            [react/text {:style style/intro-header-description}
             (i18n/label :t/created-group-chat-description
                         {:group-name chat-name})]))))))

(defn pay-to-chat-messages
  [snt-amount chat-id tribute-status tribute-label
   fiat-amount fiat-currency token]
  [tribute-to-talk.views/pay-to-chat-message
   {:snt-amount snt-amount
    :public-key chat-id
    :tribute-status tribute-status
    :tribute-label tribute-label
    :fiat-amount fiat-amount
    :fiat-currency fiat-currency
    :token token
    :style {:margin-horizontal 8
            :align-items       :flex-start
            :align-self        (if snt-amount :flex-start :flex-end)}}])

(defn one-to-one-chat-description-container
  [{:keys [chat-id name contact show-input? tribute-to-talk]
    :tribute-to-talk/keys [my-message received? message tribute-status
                           tribute-label snt-amount on-share-my-profile
                           fiat-amount fiat-currency token]}]
  (case tribute-status
    :loading
    [react/view (assoc (dissoc style/empty-chat-container :flex)
                       :justify-content :flex-end)
     [react/view {:style {:align-items :center :justify-content :flex-end}}
      [react/view {:style {:flex-direction :row :justify-content :center}}
       [react/text {:style style/loading-text}
        (i18n/label :t/loading)]
       [react/activity-indicator {:color colors/gray
                                  :animating true}]]]]

    :required
    [react/view
     [tribute-to-talk-header name]
     [pay-to-chat-messages snt-amount chat-id tribute-status tribute-label
      fiat-amount fiat-currency token]
     [react/view {:style style/are-you-friends-bubble}
      [react/text {:style (assoc style/are-you-friends-text
                                 :font-weight "500")}
       (i18n/label :t/tribute-to-talk-are-you-friends)]
      [react/text {:style style/are-you-friends-text}
       (i18n/label :t/tribute-to-talk-ask-to-be-added)]
      [react/text {:style style/share-my-profile
                   :on-press on-share-my-profile}
       (i18n/label :t/share-my-profile)]]]

    :pending
    [react/view
     [tribute-to-talk-header name]
     [pay-to-chat-messages snt-amount chat-id tribute-status tribute-label
      fiat-amount fiat-currency token]]

    (:paid :none)
    [react/view
     [intro-header contact]
     (when (= tribute-status :paid)
       [pay-to-chat-messages snt-amount chat-id tribute-status tribute-label
        fiat-amount fiat-currency token])
     (when received?
       [pay-to-chat-messages nil nil nil nil nil nil nil])

     (when (or (= tribute-status :paid) received?)
       [react/view {:style {:margin-top 16 :margin-horizontal 8}}
        [react/nested-text {:style style/tribute-received-note}
         (when received?
           [{:style (assoc style/tribute-received-note :color colors/gray)}
            (i18n/label :tribute-to-talk-tribute-received1)])
         [{:style (assoc style/tribute-received-note :font-weight "500")}
          name]
         [{:style (assoc style/tribute-received-note :color colors/gray)}
          (i18n/label (if received? :tribute-to-talk-tribute-received2
                          :tribute-to-talk-contact-received-your-tribute))]]])]

    [intro-header contact]))

(defn chat-intro-header-container
  [{:keys [group-chat name pending-invite-inviter-name
           inviter-name color chat-id chat-name public?
           contact universal-link intro-status height input-height] :as chat}
   no-messages]
  (let [icon-text  (if public? chat-id name)
        intro-name (if public? chat-name (multiaccounts/displayed-name contact))]
    ;; TODO This when check ought to be unnecessary but for now it prevents
    ;; jerky motion when fresh chat is created, when input-height can be null
    ;; affecting the calculation of content-layout-height to be briefly adjusted
    (when (or input-height
              pending-invite-inviter-name
              (not= (get-in contact [:tribute-to-talk :snt-amount]) 0))
      [react/touchable-without-feedback
       {:style    {:flex        1
                   :align-items :flex-start}
        :on-press (fn [_]
                    (re-frame/dispatch
                     [:chat.ui/set-chat-ui-props {:messages-focused? true
                                                  :show-stickers?    false}])
                    (react/dismiss-keyboard!))}
       [react/view (style/intro-header-container height intro-status no-messages)
        ;; Icon section
        [react/view {:style {:margin-top    42
                             :margin-bottom 24}}
         [chat-icon.screen/chat-intro-icon-view
          icon-text chat-id
          {:default-chat-icon      (style/intro-header-icon 120 color)
           :default-chat-icon-text style/intro-header-icon-text
           :size                   120}]]
        ;; Chat title section
        [react/text {:style style/intro-header-chat-name} (if group-chat chat-name intro-name)]
        ;; Description section
        (if group-chat
          [group-chat-description-container chat]
          [one-to-one-chat-description-container chat])]])))

(defonce messages-list-ref (atom nil))

(defview messages-view
  [{:keys [group-chat chat-id pending-invite-inviter-name contact] :as chat}
   modal?]
  (letsubs [messages           [:chats/current-chat-messages-stream]
            current-public-key [:multiaccount/public-key]]
    {:component-did-mount
     (fn [args]
       (when-not (:messages-initialized? (second (.-argv (.-props args))))
         (re-frame/dispatch [:chat.ui/load-more-messages]))
       (re-frame/dispatch [:chat.ui/set-chat-ui-props
                           {:messages-focused? true
                            :input-focused?    false}]))}
    (let [no-messages (empty? messages)
          flat-list-conf
          {:data                      messages
           :ref                       #(reset! messages-list-ref %)
           :footer                    [chat-intro-header-container chat no-messages]
           :key-fn                    #(or (:message-id %) (:value %))
           :render-fn                 (fn [message idx]
                                        [message-row
                                         {:group-chat         group-chat
                                          :modal?             modal?
                                          :current-public-key current-public-key
                                          :row                message
                                          :idx                idx
                                          :list-ref           messages-list-ref}])
           :inverted                  true
           :onEndReached              #(re-frame/dispatch [:chat.ui/load-more-messages])
           :keyboardShouldPersistTaps :handled}
          group-header {:header [group-chat-footer chat-id]}]
      (if pending-invite-inviter-name
        [list/flat-list (merge flat-list-conf group-header)]
        [list/flat-list flat-list-conf]))))

(def load-step 5)

(defn load-more [all-messages-count messages-to-load]
  (let [next-count (min all-messages-count (+ @messages-to-load load-step))]
    (reset! messages-to-load next-count)))

(defview messages-view-desktop [{:keys [chat-id group-chat]}
                                modal?]
  (letsubs [messages           [:chats/current-chat-messages-stream]
            current-public-key [:multiaccount/public-key]
            messages-to-load   (reagent/atom load-step)
            chat-id*           (reagent/atom nil)]
    {:component-did-update #(if (:messages-initialized? (second (.-argv (.-props %1))))
                              (load-more (count messages) messages-to-load)
                              (re-frame/dispatch [:chat.ui/load-more-messages]))
     :component-did-mount  #(if (:messages-initialized? (second (.-argv (.-props %1))))
                              (load-more (count messages) messages-to-load)
                              (re-frame/dispatch [:chat.ui/load-more-messages]))}
    (let [messages-list-ref    (atom nil)
          scroll-timer  (atom nil)
          scroll-height (atom nil)
          _             (when (or (not @chat-id*) (not= @chat-id* chat-id))
                          (do
                            (reset! messages-to-load load-step)
                            (reset! chat-id* chat-id)))]
      [react/view {:style style/chat-view}
       [react/scroll-view {:scrollEventThrottle              16
                           :headerHeight                     style/messages-list-vertical-padding
                           :footerWidth                      style/messages-list-vertical-padding
                           :enableArrayScrollingOptimization true
                           :inverted                         true
                           :ref                              #(reset! messages-list-ref %)
                           :on-scroll                        (fn [e]
                                                               (let [ne (.-nativeEvent e)
                                                                     y  (.-y (.-contentOffset ne))]
                                                                 (when (<= y 0)
                                                                   (when @scroll-timer (js/clearTimeout @scroll-timer))
                                                                   (reset! scroll-timer (js/setTimeout #(re-frame/dispatch [:chat.ui/load-more-messages]) 300)))
                                                                 (reset! scroll-height (+ y (.-height (.-layoutMeasurement ne))))))}
        [react/view
         (doall
          (for [{:keys [from content] :as message-obj} (take @messages-to-load messages)]
            ^{:key message-obj}
            [message-row
             {:group-chat         group-chat
              :modal?             modal?
              :current-public-key current-public-key
              :row                message-obj
              :idx                #(or (:message-id message-obj) (:value message-obj))
              :list-ref           messages-list-ref}]))]]])))

(defview chat-root [modal?]
  (letsubs [{:keys [public? chat-id chat-name show-input? group-chat contact] :as current-chat}
            [:chats/current-chat]
            current-chat-id       [:chats/current-chat-id]
            show-message-options? [:chats/current-chat-ui-prop :show-message-options?]
            show-stickers?        [:chats/current-chat-ui-prop :show-stickers?]
            two-pane-ui-enabled?  [:two-pane-ui-enabled?]
            anim-translate-y      (animation/create-value
                                   (if two-pane-ui-enabled? 0 connectivity/neg-connectivity-bar-height))]
    [react/view {:style     style/chat-view
                 :on-layout (fn [e]
                              (re-frame/dispatch [:set :layout-height (-> e .-nativeEvent .-layout .-height)]))}
     ^{:key current-chat-id}
     [status-bar/status-bar (when modal? {:type :modal-white})]
     [toolbar/toolbar
      {:chat? true
       :style {:z-index 2}}
      (if modal?
        [toolbar/nav-button
         (toolbar.actions/close toolbar.actions/default-handler)]
        toolbar/nav-back-home)
      [toolbar-content/toolbar-content-view]
      (when-not modal?
        [toolbar/actions
         [{:icon      :main-icons/more
           :icon-opts {:color               :black
                       :accessibility-label :chat-menu-button}
           :handler   #(on-options chat-id chat-name group-chat public?)}]])]
     (when-not two-pane-ui-enabled?
       [connectivity/connectivity-view anim-translate-y])
     [connectivity/connectivity-animation-wrapper
      {}
      anim-translate-y
      false
      [messages-view-animation
       (if (and (= chat-id current-chat-id) (not group-chat) (not (contact.db/added? contact)))
         [add-contact-bar chat-id])
       ;;TODO(kozieiev) : When FlatList in react-native-desktop become viable it should be used instead of optimized ScrollView for chat
       (if platform/desktop?
         [messages-view-desktop current-chat modal?]
         [messages-view current-chat modal?])]]
     (when show-input?
       [input/container])
     (when show-stickers?
       [stickers/stickers-view])
     (when show-message-options?
       [message-options/view])]))

(defview chat []
  [chat-root false])

(defview chat-modal []
  [chat-root true])

(defview select-chat []
  [react/view {:align-items :center :justify-content :center :flex 1}
   [react/text style/select-chat
    (i18n/label :t/select-chat)]])
