(ns status-im.ui.screens.chat.views
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models :as models.chat]
            [status-im.contact.db :as contact.db]
            [status-im.group-chats.db :as group-chats.db]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.button.view :as buttons]
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
            [status-im.ui.screens.chat.bottom-info :as bottom-info]
            [status-im.ui.screens.chat.input.input :as input]
            [status-im.ui.screens.chat.message.datemark :as message-datemark]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.screens.chat.message.options :as message-options]
            [status-im.ui.screens.chat.stickers.views :as stickers]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.ui.screens.chat.toolbar-content :as toolbar-content]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [status-im.utils.datetime :as datetime])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn add-contact-bar [public-key]
  [react/touchable-highlight
   {:on-press
    #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])
    :accessibility-label :add-to-contacts-button
    :style style/add-contact}
   [react/view style/add-contact-center
    [vector-icons/icon :main-icons/add
     {:color colors/blue}]
    [react/i18n-text {:style style/add-contact-text :key :add-to-contacts}]]])

(defn- on-options [chat-id chat-name group-chat? public?]
  (list-selection/show {:title   chat-name
                        :options (actions/actions group-chat? chat-id public?)}))

(defn chat-toolbar [{:keys [chat-name group-chat chat-id contact]} public? modal?]
  [react/view
   [status-bar/status-bar (when modal? {:type :modal-white})]
   [toolbar/toolbar
    {:chat? true}
    (if modal?
      [toolbar/nav-button
       (toolbar.actions/close toolbar.actions/default-handler)]
      toolbar/nav-back-home)
    [toolbar-content/toolbar-content-view]
    (when-not modal?
      [toolbar/actions [{:icon      :main-icons/more
                         :icon-opts {:color               :black
                                     :accessibility-label :chat-menu-button}
                         :handler   #(on-options chat-id chat-name group-chat public?)}]])]
   [connectivity/connectivity-view]
   (when (and (not group-chat)
              (not (contact.db/added? contact)))
     [add-contact-bar chat-id])])

(defmulti message-row (fn [{{:keys [type]} :row}] type))

(defmethod message-row :datemark
  [{{:keys [value]} :row}]
  [message-datemark/chat-datemark-mobile value])

(defview gap
  [ids idx list-ref in-progress? connected? range first-gap? intro-loading?]
  (when-not (and first-gap? intro-loading?)
    [react/view {:align-self          :stretch
                 :margin-top          24
                 :margin-bottom       24
                 :height              48
                 :align-items         :center
                 :justify-content     :center
                 :border-color        colors/gray-light
                 :border-top-width    1
                 :border-bottom-width 1
                 :background-color    :white}
     [react/touchable-highlight
      {:on-press (when (and connected? (not in-progress?))
                   #(do
                      (when @list-ref
                        (.scrollToIndex @list-ref #js {:index        (max 0 (dec idx))
                                                       :viewOffset   20
                                                       :viewPosition 0.5}))
                      (if first-gap?
                        (re-frame/dispatch [:chat.ui/fetch-more])
                        (re-frame/dispatch [:chat.ui/fill-gaps ids]))))}
      [react/view {:style {:flex            1
                           :align-items     :center
                           :justify-content :center
                           :text-align      :center}}
       (if in-progress?
         [react/activity-indicator]
         [react/nested-text
          {:style {:text-align :center
                   :color      (if connected?
                                 colors/blue
                                 colors/gray)}}
          (i18n/label (if first-gap?
                        :t/load-more-messages
                        :t/fetch-messages))
          (when first-gap?
            [{:style {:typography :caption
                      :color      colors/gray}}
             (str "\n"
                  (i18n/label :t/load-messages-before
                              {:date (datetime/timestamp->long-date
                                      (* 1000 (:lowest-request-from range)))}))])])]]]))

(defview gap-wrapper [{:keys [gaps first-gap?]} idx list-ref]
  (letsubs [in-progress? [:chats/fetching-gap-in-progress?
                          (if first-gap?
                            [:first-gap]
                            (:ids gaps))]
            connected?   [:mailserver/connected?]
            range        [:chats/range]
            intro-status [:chats/current-chat-intro-status]]
    [gap (:ids gaps) idx list-ref in-progress?
     connected? range first-gap? (= intro-status :loading)]))

(defmethod message-row :gap
  [{:keys [row idx list-ref]}]
  [gap-wrapper row idx list-ref])

(defmethod message-row :default
  [{:keys [group-chat current-public-key modal? row]}]
  [message/chat-message (assoc row
                               :group-chat group-chat
                               :modal? modal?
                               :current-public-key current-public-key)])

(def animation-duration 200)

(defview messages-view-animation [message-view]
  ;; smooths out appearance of message-view
  (letsubs [opacity (animation/create-value 0)]
    {:component-did-mount (fn [_]
                            (animation/start
                             (animation/timing
                              opacity
                              {:toValue         1
                               :duration        animation-duration
                               :useNativeDriver true})))}
    [react/with-activity-indicator
     {:style   style/message-view-preview
      :preview [react/view style/message-view-preview]}
     [react/touchable-without-feedback
      {:on-press (fn [_]
                   (re-frame/dispatch [:chat.ui/set-chat-ui-props {:messages-focused? true
                                                                   :show-stickers? false}])
                   (react/dismiss-keyboard!))}
      [react/animated-view {:style (style/message-view-animated opacity)}
       message-view]]]))

(defn join-chat-button [chat-id]
  [buttons/secondary-button {:style style/join-button
                             :on-press #(re-frame/dispatch [:group-chats.ui/join-pressed chat-id])}
   (i18n/label :t/join-group-chat)])

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
    [chat-icon.screen/profile-icon-view nil name color false 100 {:default-chat-icon-text style/group-chat-icon}]]
   [react/view {:style style/group-chat-join-footer}
    [react/view {:style style/group-chat-join-container}
     [react/view
      [react/text {:style style/group-chat-join-name} name]]
     [react/text {:style style/intro-header-description}
      (i18n/label :t/join-group-chat-description {:username inviter-name
                                                  :group-name name})]
     [join-chat-button chat-id]
     [decline-chat chat-id]]]])

;; TODO refactor this big view into chunks
(defview chat-intro-header-container
  [{:keys [group-chat name pending-invite-inviter-name
           inviter-name color chat-id chat-name public?
           universal-link]} no-messages]
  (letsubs [intro-status [:chats/current-chat-intro-status]
            height       [:chats/content-layout-height]
            input-height [:chats/current-chat-ui-prop :input-height]
            {:keys [:lowest-request-from :highest-request-to]} [:chats/range]]
    (let [icon-text  (if public? chat-id name)
          intro-name (if public? chat-name name)]
      ;; TODO This when check ought to be unnecessary but for now it prevents
      ;; jerky motion when fresh chat is created, when input-height can be null
      ;; affecting the calculation of content-layout-height to be briefly adjusted
      (when (or input-height pending-invite-inviter-name)
        [react/touchable-without-feedback
         {:style    {:flex        1
                     :align-items :flex-start}
          :on-press (fn [_]
                      (re-frame/dispatch
                       [:chat.ui/set-chat-ui-props {:messages-focused? true
                                                    :show-stickers?    false}])
                      (react/dismiss-keyboard!))}
         [react/view
          (style/intro-header-container height intro-status no-messages)
          ;; Icon section
          [react/view {:style {:margin-top    42
                               :margin-bottom 24}}
           [chat-icon.screen/chat-intro-icon-view
            icon-text chat-id
            {:default-chat-icon      (style/intro-header-icon 120 color)
             :default-chat-icon-text style/intro-header-icon-text
             :size                   120}]]
          ;; Chat title section
          [react/text {:style style/intro-header-chat-name} intro-name]
          ;; Description section
          (if group-chat
            (cond
              (= intro-status :loading)
              [react/view {:style (merge style/intro-header-description-container
                                         {:margin-bottom 36
                                          :height        44})}
               [react/text {:style style/intro-header-description}
                (i18n/label :t/loading)]
               [react/activity-indicator {:animating true
                                          :size      :small
                                          :color     colors/gray}]]

              (= intro-status :empty)
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

              (= intro-status :messages)
              (when (not public?)
                (if pending-invite-inviter-name
                  [react/nested-text {:style style/intro-header-description}
                   [{:style {:color :black}} pending-invite-inviter-name]
                   (i18n/label :t/join-group-chat-description
                               {:username   ""
                                :group-name intro-name})]
                  (if (not= inviter-name "Unknown")
                    [react/nested-text {:style style/intro-header-description}
                     (i18n/label :t/joined-group-chat-description
                                 {:username   ""
                                  :group-name intro-name})
                     [{:style {:color :black}} inviter-name]]
                    [react/text {:style style/intro-header-description}
                     (i18n/label :t/created-group-chat-description
                                 {:group-name intro-name})]))))
            [react/nested-text {:style (merge style/intro-header-description
                                              {:margin-bottom 36})}
             (i18n/label :t/empty-chat-description-one-to-one)
             [{} intro-name]])]]))))

(defonce messages-list-ref (atom nil))

(defview messages-view
  [{:keys [group-chat chat-id pending-invite-inviter-name] :as chat}
   modal?]
  (letsubs [messages           [:chats/current-chat-messages-stream]
            current-public-key [:account/public-key]]
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
           :onEndReached              #(re-frame/dispatch
                                        [:chat.ui/load-more-messages])
           :enableEmptySections       true
           :keyboardShouldPersistTaps :handled}
          group-header {:header [group-chat-footer chat-id]}]
      (if pending-invite-inviter-name
        [list/flat-list (merge flat-list-conf group-header)]
        [list/flat-list flat-list-conf]))))

(defn show-input-container? [my-public-key current-chat]
  (or (not (models.chat/group-chat? current-chat))
      (group-chats.db/joined? my-public-key current-chat)))

(defview chat-root [modal?]
  (letsubs [{:keys [public? chat-id] :as current-chat} [:chats/current-chat]
            current-chat-id                            [:chats/current-chat-id]
            my-public-key                              [:account/public-key]
            show-bottom-info?                          [:chats/current-chat-ui-prop :show-bottom-info?]
            show-message-options?                      [:chats/current-chat-ui-prop :show-message-options?]
            show-stickers?                             [:chats/current-chat-ui-prop :show-stickers?]]
    ;; this check of current-chat-id is necessary only because in a fresh public chat creation sometimes
    ;; this component renders before current-chat-id is set to current chat-id. Hence further down in sub
    ;; components (e.g. chat-toolbar) there can be a brief visual inconsistancy like showing 'add contact'
    ;; in public chat
    (when (= chat-id current-chat-id)
      ;; this scroll-view is a hack that allows us to use on-blur and on-focus on Android
      ;; more details here: https://github.com/facebook/react-native/issues/11071
      [react/scroll-view {:scroll-enabled               false
                          :style                        style/scroll-root
                          :content-container-style      style/scroll-root
                          :keyboard-should-persist-taps :handled}
       ^{:key current-chat-id}
       [react/view {:style     style/chat-view
                    :on-layout (fn [e]
                                 (re-frame/dispatch [:set :layout-height (-> e .-nativeEvent .-layout .-height)]))}
        [chat-toolbar current-chat public? modal?]
        [messages-view-animation
         [messages-view current-chat modal?]]
        (when (show-input-container? my-public-key current-chat)
          [input/container])
        (when show-stickers?
          [stickers/stickers-view])
        (when show-bottom-info?
          [bottom-info/bottom-info-view])
        (when show-message-options?
          [message-options/view])]])))

(defview chat []
  [chat-root false])

(defview chat-modal []
  [chat-root true])
