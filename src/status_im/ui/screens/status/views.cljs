(ns status-im.ui.screens.status.views
  (:require [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.chat.models :as chat]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.fast-image :as fast-image]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.plus-button :as components.plus-button]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.tabs :as tabs]
            [status-im.ui.screens.chat.components.reply :as components.reply]
            [status-im.ui.screens.chat.image.preview.views :as preview]
            [status-im.ui.screens.chat.message.gap :as gap]
            [status-im.ui.screens.chat.message.link-preview :as link-preview]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.screens.chat.message.reactions-old :as reactions]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.status.styles :as styles]
            [utils.datetime :as datetime]))

(defonce messages-list-ref (atom nil))
(def image-max-dimension 192)

(defn image-set-size
  [width]
  (fn [evt]
    (reset! width (/ (.-width (.-nativeEvent evt))
                     (/ (.-height (.-nativeEvent evt)) image-max-dimension)))))

(defn message-content-image
  [_ _]
  (let [width (reagent/atom nil)]
    (fn [uri show-close?]
      [react/view
       {:style               {:width         @width
                              :align-items   :center
                              :height        image-max-dimension
                              :max-width     :100%
                              :overflow      :hidden
                              :opacity       (if @width 1 0)
                              :border-radius 16
                              :margin-top    8}
        :accessibility-label :image-message}
       [fast-image/fast-image
        {:style   {:width  @width
                   :height image-max-dimension}
         :on-load (image-set-size width)
         :source  {:uri uri}}]
       [react/view
        {:border-width     1
         :top              0
         :left             0
         :width            @width
         :height           image-max-dimension
         :border-radius    16
         :position         :absolute
         :background-color :transparent
         :border-color     colors/black-transparent}]
       (when show-close?
         [react/touchable-highlight
          {:on-press            #(re-frame/dispatch [:chat.ui/cancel-sending-image-timeline])
           :accessibility-label :cancel-send-image
           :style               {:right 4 :top 12 :position :absolute}}
          [react/view
           {:width            24
            :height           24
            :background-color colors/black-persist
            :border-radius    12
            :align-items      :center
            :justify-content  :center}
           [icons/icon :main-icons/close-circle {:color colors/white-persist}]]])])))

(defn on-long-press-fn
  [on-long-press content image]
  (on-long-press
   (when-not image
     [{:id       :copy
       :on-press #(react/copy-to-clipboard
                   (components.reply/get-quoted-text-with-mentions
                    (get content :parsed-text)))
       :label    (i18n/label :t/sharing-copy-to-clipboard)}])))

(defn image-message
  []
  (let [visible (reagent/atom false)]
    (fn [{:keys [content] :as message} on-long-press]
      [:<>
       [preview/preview-image
        {:message   (assoc message :cant-be-replied true)
         :visible   @visible
         :can-reply false
         :on-close  #(do (reset! visible false)
                         (reagent/flush))}]
       [react/touchable-highlight
        {:on-press      (fn [_]
                          (reset! visible true)
                          (react/dismiss-keyboard!))
         :on-long-press #(on-long-press-fn on-long-press content true)}
        [message-content-image (:image content) false]]])))

(defn message-item
  [account profile]
  (fn [{:keys [content-type content from timestamp outgoing] :as message}
       {:keys [modal on-long-press close-modal]}]

    [react/view
     (merge {:padding-vertical   8
             :flex-direction     :row
             :background-color   (when modal colors/white)
             :padding-horizontal 16}
            (when modal
              {:border-radius 16}))
     [react/touchable-highlight
      {:on-press #(do (when modal (close-modal))
                      (when profile (re-frame/dispatch [:navigate-back]))
                      (re-frame/dispatch [:chat.ui/show-profile from]))}
      [react/view {:padding-top 2 :padding-right 8}
       (if outgoing
         [photos/account-photo account]
         [photos/member-photo from])]]
     [react/view {:flex 1}
      [react/view
       {:flex-direction  :row
        :justify-content :space-between
        :width           :98%
        :word-break      :break-all}
       [react/touchable-highlight
        {:on-press #(do (when modal (close-modal))
                        (when profile (re-frame/dispatch [:navigate-back]))
                        (re-frame/dispatch [:chat.ui/show-profile from]))}
        (let [message-author-width (* @(re-frame/subscribe [:dimensions/window-width]) 0.75)]
          (if outgoing
            [react/view {:style {:width message-author-width}}
             [message/message-my-name {:profile? true :you? false}]]
            [react/view {:style {:width message-author-width}}
             [message/message-author-name from {:profile? true}]]))]
       [react/text {:style {:font-size 10 :color colors/gray :margin-left :auto}}
        (datetime/time-ago (datetime/to-date timestamp))]]
      [react/view
       (if (= content-type constants/content-type-image)
         [image-message message on-long-press]
         [react/touchable-highlight
          (when-not modal
            {:on-long-press #(on-long-press-fn on-long-press content false)})
          [message/render-parsed-text (assoc message :outgoing false) (:parsed-text content)]])
       [link-preview/link-preview-wrapper (:links content) outgoing true]]]]))

(defn render-message
  [{:keys [type] :as message} idx _ {:keys [timeline account chat-id profile]}]
  (if (= type :datemark)
    nil
    (if (= type :gap)
      (if timeline
        nil
        [gap/gap message idx messages-list-ref true chat-id])
      ;; for timeline for reactions we need to use :from as chat-id
      (let [chat-id (chat/profile-chat-topic (:from message))]
        [react/view
         (merge {:accessibility-label :chat-item}
                (when (:last-in-group? message)
                  {:padding-bottom      8
                   :margin-bottom       8
                   :border-bottom-width 1
                   :border-bottom-color colors/gray-lighter}))
         [reactions/with-reaction-picker
          {:message         message
           :timeline        true
           :reactions       @(re-frame/subscribe [:chats/message-reactions (:message-id message)
                                                  constants/timeline-chat-id])
           :picker-on-open  (fn [])
           :picker-on-close (fn [])
           :send-emoji      (fn [{:keys [emoji-id]}]
                              (re-frame/dispatch [:models.reactions/send-emoji-reaction
                                                  {:message-id (:message-id message)
                                                   :chat-id    chat-id
                                                   :emoji-id   emoji-id}]))
           :retract-emoji   (fn [{:keys [emoji-id emoji-reaction-id]}]
                              (re-frame/dispatch [:models.reactions/send-emoji-reaction-retraction
                                                  {:message-id        (:message-id message)
                                                   :chat-id           chat-id
                                                   :emoji-id          emoji-id
                                                   :emoji-reaction-id emoji-reaction-id}]))
           :render          (message-item account profile)}]]))))

(def state (reagent/atom {:tab :timeline}))

(defn tabs
  []
  (let [{:keys [tab]} @state]
    [react/view
     {:flex-direction     :row
      :padding-horizontal 4
      :margin-top         8}
     [tabs/tab-title state :timeline (i18n/label :t/timeline) (= tab :timeline)]
     [tabs/tab-title state :status (i18n/label :t/my-status) (= tab :status)]]))

(defn timeline
  []
  (let [messages          @(re-frame/subscribe [:chats/timeline-messages-stream])
        loading-messages? @(re-frame/subscribe [:chats/loading-messages? constants/timeline-chat-id])
        no-messages?      @(re-frame/subscribe [:chats/chat-no-messages? constants/timeline-chat-id])
        account           @(re-frame/subscribe [:multiaccount])]
    [react/view {:flex 1}
     [react/view
      {:height           1
       :background-color colors/gray-lighter}]
     (if (and no-messages? loading-messages?)
       [react/view {:flex 1 :align-items :center :justify-content :center}
        [react/activity-indicator {:animating true}]]
       (if no-messages?
         [react/view
          {:padding-horizontal 32
           :margin-top         64}
          [fast-image/fast-image
           {:style  {:width      140
                     :height     140
                     :align-self :center}
            :source
            {:uri
             "https://bafybeieayj76s4vjlw5uwdvnakosy46rqyioqsp2ygl6sedivemhkxrbwi.ipfs.cf-ipfs.com"}}]
          [react/view (styles/descr-container)
           [react/text
            {:style {:color       colors/gray
                     :line-height 22}}
            (if (= :timeline (:tab @state))
              (i18n/label :t/statuses-descr)
              (i18n/label :t/statuses-my-status-descr))]]]
         [list/flat-list
          {:key-fn                    #(or (:message-id %) (:value %))
           :render-data               {:timeline (= :timeline (:tab @state))
                                       :account  account}
           :render-fn                 render-message
           :data                      messages
           :on-end-reached            #(re-frame/dispatch [:chat.ui/load-more-messages
                                                           constants/timeline-chat-id])
           ;;don't remove :on-scroll-to-index-failed
           :on-scroll-to-index-failed #()
           :header                    [react/view {:height 8}]
           :footer                    [react/view {:height 68}]}]))
     [components.plus-button/plus-button-old
      {:on-press #(re-frame/dispatch [:open-modal :my-status])}]]))
