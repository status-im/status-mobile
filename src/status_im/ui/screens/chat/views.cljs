(ns status-im.ui.screens.chat.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.sheets :as sheets]
            [quo.animated :as animated]
            [quo.react-native :as rn]
            [quo.platform :as platform]
            [status-im.ui.screens.chat.audio-message.views :as audio-message]
            [quo.react :as quo.react]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.screens.chat.stickers.views :as stickers]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.ui.screens.chat.toolbar-content :as toolbar-content]
            [status-im.ui.screens.chat.image.views :as image]
            [status-im.ui.screens.chat.state :as state]
            [status-im.utils.debounce :as debounce]
            [status-im.ui.screens.chat.extensions.views :as extensions]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.chat.group :as chat.group]
            [status-im.ui.screens.chat.message.gap :as gap]
            [status-im.ui.components.invite.chat :as invite.chat]
            [status-im.ui.screens.chat.components.accessory :as accessory]
            [status-im.ui.screens.chat.components.input :as components]
            [status-im.ui.screens.chat.message.datemark :as message-datemark]
            [status-im.ui.components.toolbar :as toolbar]
            [quo.core :as quo]
            [clojure.string :as string]
            [status-im.constants :as constants]))

(defn topbar []
  (let [current-chat @(re-frame/subscribe [:current-chat/metadata])]
    [topbar/topbar
     {:content           [toolbar-content/toolbar-content-view current-chat]
      :navigation        {:on-press #(re-frame/dispatch [:navigate-to :home])}
      :right-accessories [{:icon                :main-icons/more
                           :accessibility-label :chat-menu-button
                           :on-press
                           #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                {:content (fn []
                                                            [sheets/actions current-chat])
                                                 :height  256}])}]}]))

(defn invitation-requests [chat-id admins]
  (let [current-pk @(re-frame/subscribe [:multiaccount/public-key])
        admin? (get admins current-pk)]
    (when admin?
      (let [invitations @(re-frame/subscribe [:group-chat/pending-invitations-by-chat-id chat-id])]
        (when (seq invitations)
          [react/touchable-highlight
           {:on-press            #(re-frame/dispatch [:navigate-to :group-chat-invite])
            :accessibility-label :invitation-requests-button}
           [react/view {:style (style/add-contact)}
            [react/text {:style style/add-contact-text}
             (i18n/label :t/group-membership-request)]]])))))

(defn add-contact-bar [public-key]
  (let [added? @(re-frame/subscribe [:contacts/contact-added? public-key])]
    (when-not added?
      [react/touchable-highlight
       {:on-press
        #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])
        :accessibility-label :add-to-contacts-button}
       [react/view {:style (style/add-contact)}
        [vector-icons/icon :main-icons/add
         {:color colors/blue}]
        [react/i18n-text {:style style/add-contact-text :key :add-to-contacts}]]])))

(defn chat-intro [{:keys [chat-id
                          chat-name
                          chat-type
                          group-chat
                          invitation-admin
                          contact-name
                          color
                          loading-messages?
                          no-messages?]}]
  [react/view (style/intro-header-container loading-messages? no-messages?)
   ;; Icon section
   [react/view {:style {:margin-top    42
                        :margin-bottom 24}}
    [chat-icon.screen/chat-intro-icon-view
     chat-name chat-id group-chat
     {:default-chat-icon      (style/intro-header-icon 120 color)
      :default-chat-icon-text style/intro-header-icon-text
      :size                   120}]]
   ;; Chat title section
   [react/text {:style (style/intro-header-chat-name)}
    (if group-chat chat-name contact-name)]
   ;; Description section
   (if group-chat
     [chat.group/group-chat-description-container {:chat-id chat-id
                                                   :invitation-admin invitation-admin
                                                   :loading-messages? loading-messages?
                                                   :chat-name chat-name
                                                   :chat-type chat-type
                                                   :no-messages? no-messages?}]
     [react/text {:style (assoc style/intro-header-description
                                :margin-bottom 32)}

      (str
       (i18n/label :t/empty-chat-description-one-to-one)
       contact-name)])])

(defn chat-intro-one-to-one [{:keys [chat-id] :as opts}]
  (let [contact-names @(re-frame/subscribe
                        [:contacts/contact-two-names-by-identity chat-id])]
    (chat-intro (assoc opts :contact-name (first contact-names)))))

(defn chat-intro-header-container
  [{:keys [group-chat invitation-admin
           chat-type
           might-have-join-time-messages?
           color chat-id chat-name
           public?]}
   no-messages]
  [react/touchable-without-feedback
   {:style    {:flex        1
               :align-items :flex-start}
    :on-press (fn [_]
                (react/dismiss-keyboard!))}
   (let [opts
         {:chat-id chat-id
          :group-chat group-chat
          :invitation-admin invitation-admin
          :chat-type chat-type
          :chat-name chat-name
          :public? public?
          :color color
          :loading-messages? might-have-join-time-messages?
          :no-messages? no-messages}]
     (if group-chat
       [chat-intro opts]
       [chat-intro-one-to-one opts]))])

(defonce messages-list-ref (atom nil))

(defn on-viewable-items-changed [^js e]
  (when @messages-list-ref
    (reset! state/first-not-visible-item
            (when-let [^js last-visible-element (aget (.-viewableItems e) (dec (.-length ^js (.-viewableItems e))))]
              (let [index (.-index last-visible-element)
                    ;; Get first not visible element, if it's a datemark/gap
                    ;; we might unnecessarely add messages on receiving as
                    ;; they do not have a clock value, but most of the times
                    ;; it will be a message
                    first-not-visible (aget (.-data ^js (.-props ^js @messages-list-ref)) (inc index))]
                (when (and first-not-visible
                           (= :message (:type first-not-visible)))
                  first-not-visible)))))
  (debounce/debounce-and-dispatch [:chat.ui/message-visibility-changed e] 5000))

(defn render-fn [{:keys [outgoing type] :as message} idx _ {:keys [group-chat public? current-public-key space-keeper]}]
  [react/view {:style (when platform/android? {:scaleY -1})}
   (if (= type :datemark)
     [message-datemark/chat-datemark (:value message)]
     (if (= type :gap)
       [gap/gap message idx messages-list-ref false]
       ; message content
       [message/chat-message
        (assoc message
               :incoming-group (and group-chat (not outgoing))
               :group-chat group-chat
               :public? public?
               :current-public-key current-public-key)
        space-keeper]))])

(defn messages-view
  [{:keys [chat bottom-space pan-responder space-keeper]}]
  (let [{:keys [group-chat chat-id chat-type public? invitation-admin]} chat

        messages           @(re-frame/subscribe [:chats/current-chat-messages-stream])
        no-messages?       @(re-frame/subscribe [:chats/current-chat-no-messages?])
        current-public-key @(re-frame/subscribe [:multiaccount/public-key])]
    [list/flat-list
     (merge
      pan-responder
      {:key-fn                       #(or (:message-id %) (:value %))
       :ref                          #(reset! messages-list-ref %)
       :header                       (when (= chat-type constants/private-group-chat-type)
                                       [react/view {:style (when platform/android? {:scaleY -1})}
                                        [chat.group/group-chat-footer chat-id invitation-admin]])
       :footer                       [react/view {:style (when platform/android? {:scaleY -1})}
                                      [chat-intro-header-container chat no-messages?]
                                      (when (= chat-type constants/one-to-one-chat-type)
                                        [invite.chat/reward-messages])]
       :data                         messages
       ;;TODO https://github.com/facebook/react-native/issues/30034
       :inverted                     (when platform/ios? true)
       :style                        (when platform/android? {:scaleY -1})
       :render-data                  {:group-chat         group-chat
                                      :public?            public?
                                      :current-public-key current-public-key
                                      :space-keeper       space-keeper}
       :render-fn                    render-fn
       :on-viewable-items-changed    on-viewable-items-changed
       :on-end-reached               #(re-frame/dispatch [:chat.ui/load-more-messages])
       :on-scroll-to-index-failed    #() ;;don't remove this
       :content-container-style      {:padding-top    (+ bottom-space 16)
                                      :padding-bottom 16}
       :scrollIndicatorInsets        {:top bottom-space}
       :keyboardDismissMode          "interactive"
       :keyboard-should-persist-taps :handled})]))

(defn bottom-sheet [input-bottom-sheet]
  (case input-bottom-sheet
    :stickers
    [stickers/stickers-view]
    :extensions
    [extensions/extensions-view]
    :images
    [image/image-view]
    :audio
    [audio-message/audio-message-view]
    nil))

(defn invitation-bar [chat-id]
  (let [{:keys [state chat-id] :as invitation}
        (first @(re-frame/subscribe [:group-chat/invitations-by-chat-id chat-id]))
        {:keys [retry? message]} @(re-frame/subscribe [:chats/current-chat-membership])]
    [react/view {:margin-horizontal 16 :margin-top 10}
     (cond
       (and invitation (= constants/invitation-state-requested state) (not retry?))
       [toolbar/toolbar {:show-border? true
                         :center
                         [quo/button
                          {:type     :secondary
                           :disabled true}
                          (i18n/label :t/request-pending)]}]

       (and invitation (= constants/invitation-state-rejected state) (not retry?))
       [toolbar/toolbar {:show-border? true
                         :right
                         [quo/button
                          {:type     :secondary
                           :accessibility-label :retry-button
                           :on-press #(re-frame/dispatch [:group-chats.ui/membership-retry])}
                          (i18n/label :t/mailserver-retry)]
                         :left
                         [quo/button
                          {:type     :secondary
                           :accessibility-label :remove-group-button
                           :on-press #(re-frame/dispatch [:group-chats.ui/remove-chat-confirmed chat-id])}
                          (i18n/label :t/remove-group)]}]
       :else
       [toolbar/toolbar {:show-border? true
                         :center
                         [quo/button
                          {:type                :secondary
                           :accessibility-label :introduce-yourself-button
                           :disabled            (string/blank? message)
                           :on-press            #(re-frame/dispatch [:send-group-chat-membership-request])}
                          (i18n/label :t/request-membership)]}])]))

(defn chat []
  (let [bottom-space   (reagent/atom 0)
        panel-space    (reagent/atom 0)
        active-panel   (reagent/atom nil)
        position-y     (animated/value 0)
        pan-state      (animated/value 0)
        text-input-ref (quo.react/create-ref)
        on-update      (partial reset! panel-space)
        pan-responder  (accessory/create-pan-responder position-y pan-state)
        space-keeper   (fn [state]
                         ;; NOTE: Only iOs now because we use soft input resize screen on android
                         (when platform/ios?
                           (cond
                             (and state
                                  (< @bottom-space @panel-space)
                                  (not @active-panel))
                             (reset! bottom-space @panel-space)

                             (and (not state)
                                  (< @panel-space @bottom-space))
                             (do
                               (some-> ^js (quo.react/current-ref text-input-ref) .focus)
                               (reset! panel-space @bottom-space)
                               (reset! bottom-space 0)))))
        set-active-panel (fn [panel]
                           (rn/configure-next
                            (:ease-opacity-200 rn/custom-animations))
                           (reset! active-panel panel)
                           (reagent/flush)
                           (when panel
                             (js/setTimeout #(react/dismiss-keyboard!) 100)))
        on-text-change #(re-frame/dispatch [:chat.ui/set-chat-input-text %])]
    (fn []
      (let [{:keys [chat-id show-input? group-chat admins invitation-admin] :as current-chat}
            @(re-frame/subscribe [:chats/current-chat])]
        (when current-chat
          [react/view {:style {:flex 1}}
           [connectivity/loading-indicator]
           [topbar]
           [react/view {:style {:flex 1}}
            (if group-chat
              [invitation-requests chat-id admins]
              [add-contact-bar chat-id])
            [messages-view {:chat          current-chat
                            :bottom-space  (max @bottom-space @panel-space)
                            :pan-responder pan-responder
                            :space-keeper  space-keeper}]]
           (when (and group-chat invitation-admin)
             [accessory/view {:y               position-y
                              :on-update-inset on-update}
              [invitation-bar chat-id]])
           ;; NOTE(rasom): on android we have to place `autocomplete-mentions`
           ;; outside `accessory/view` because otherwise :keyboardShouldPersistTaps
           ;; :always doesn't work and keyboard is hidden on pressing suggestion.
           ;; Scrolling of suggestions doesn't work neither in this case.
           (when platform/android?
             [components/autocomplete-mentions text-input-ref])
           (when show-input?
             [accessory/view {:y               position-y
                              :pan-state       pan-state
                              :has-panel       (boolean @active-panel)
                              :on-close        #(set-active-panel nil)
                              :on-update-inset on-update}
              [components/chat-toolbar
               {:active-panel             @active-panel
                :set-active-panel         set-active-panel
                :text-input-ref           text-input-ref
                :on-text-change           on-text-change}]
              [bottom-sheet @active-panel]])])))))
