(ns status-im2.contexts.chat.messages.drawers.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im.ui.components.react :as react]
            [status-im2.contexts.chat.composer.reply.view :as reply]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [status-im2.common.contact-list-item.view :as contact-list-item]
            [status-im2.contexts.chat.messages.drawers.style :as style]
            [react-native.gesture :as gesture]
            [quo2.components.reactions.resource :as reactions.resource]))

(defn contact-list-item-fn
  [{:keys [from compressed-key]}]
  (let [[primary-name secondary-name] (rf/sub [:contacts/contact-two-names-by-identity
                                               from])
        {:keys [ens-verified added?]} (rf/sub [:contacts/contact-by-address from])]
    ^{:key compressed-key}
    [contact-list-item/contact-list-item
     {:on-press #(rf/dispatch [:chat.ui/show-profile from])}
     {:primary-name   primary-name
      :secondary-name secondary-name
      :public-key     from
      :compressed-key compressed-key
      :ens-verified   ens-verified
      :added?         added?}]))

(defn- get-tabs-data
  [{:keys [reaction-authors selected-tab reactions-order theme]}]
  (map (fn [reaction-type-int]
         (let [author-details (get reaction-authors reaction-type-int)]
           {:id                  reaction-type-int
            :accessibility-label (keyword (str "authors-for-reaction-" reaction-type-int))
            :label               [rn/view {:style style/tab}
                                  [rn/image
                                   {:source (reactions.resource/get-reaction
                                             (get constants/reactions reaction-type-int))
                                    :style  style/tab-icon}]
                                  [quo/text
                                   {:weight :medium
                                    :size   :paragraph-1
                                    :style  (style/tab-count (= selected-tab
                                                                reaction-type-int)
                                                             theme)}
                                   (count author-details)]]}))
       reactions-order))

(defn- reaction-authors-comp
  [{:keys [selected-tab reaction-authors reactions-order theme]}]
  [:<>
   [rn/view style/tabs-container
    [quo/tabs
     {:size            32
      :scrollable?     true
      :in-scroll-view? true
      :on-change       #(reset! selected-tab %)
      :default-active  @selected-tab
      :data            (get-tabs-data {:reaction-authors reaction-authors
                                       :selected-tab     @selected-tab
                                       :reactions-order  reactions-order
                                       :theme            theme})}]]
   [gesture/flat-list
    {:data      (for [contact (get reaction-authors @selected-tab)]
                  contact)
     :render-fn contact-list-item-fn
     :key-fn    :from
     :style     style/authors-list}]])

(defn reaction-authors
  [{:keys [reactions-order theme]}]
  (let [{:keys [reaction-authors-list
                selected-reaction]} (rf/sub [:chat/reactions-authors])
        selected-tab                (reagent/atom (or selected-reaction
                                                      (first (keys reaction-authors-list))))]
    (fn []
      [reaction-authors-comp
       {:selected-tab     selected-tab
        :reaction-authors reaction-authors-list
        :reactions-order  reactions-order
        :theme            theme}])))

(defn pin-message
  [{:keys [chat-id pinned pinned-by] :as message-data}]
  (let [pinned-messages     (rf/sub [:chats/pinned chat-id])
        message-not-pinned? (and (empty? pinned-by) (not pinned))]
    (if (and message-not-pinned? (> (count pinned-messages) 2))
      (do
        (js/setTimeout (fn [] (rf/dispatch [:dismiss-keyboard])) 500)
        (rf/dispatch [:pin-message/show-pin-limit-modal chat-id]))
      (rf/dispatch [:pin-message/send-pin-message
                    (assoc message-data :pinned message-not-pinned?)]))))

(defn get-actions
  [{:keys [outgoing content pinned-by outgoing-status deleted? deleted-for-me? content-type]
    :as   message-data}
   {:keys [able-to-send-message? community? can-delete-message-for-everyone?
           message-pin-enabled group-chat group-admin?]}]
  (concat
   (when (and outgoing
              (not (or deleted? deleted-for-me?))
              ;; temporarily disable edit image message until
              ;; https://github.com/status-im/status-mobile/issues/15298 is implemented
              (not= content-type constants/content-type-image)
              (not= content-type constants/content-type-audio))
     [{:type     :main
       :on-press #(rf/dispatch [:chat.ui/edit-message message-data])
       :label    (i18n/label :t/edit-message)
       :icon     :i/edit
       :id       :edit}])
   (when (and able-to-send-message? (not= outgoing-status :sending) (not (or deleted? deleted-for-me?)))
     [{:type     :main
       :on-press #(rf/dispatch [:chat.ui/reply-to-message message-data])
       :label    (i18n/label :t/message-reply)
       :icon     :i/reply
       :id       :reply}])
   (when (and (not (or deleted? deleted-for-me?))
              (not= content-type constants/content-type-audio))
     [{:type     :main
       :on-press #(react/copy-to-clipboard
                   (reply/get-quoted-text-with-mentions
                    (get content :parsed-text)))
       :label    (i18n/label :t/copy-text)
       :icon     :i/copy
       :id       :copy}])
   ;; pinning images are temporarily disabled
   (when (and message-pin-enabled
              (not= content-type constants/content-type-image))
     [{:type     :main
       :on-press #(pin-message message-data)
       :label    (i18n/label (if pinned-by
                               (if community? :t/unpin-from-channel :t/unpin-from-chat)
                               (if community? :t/pin-to-channel :t/pin-to-chat)))
       :icon     :i/pin
       :id       (if pinned-by :unpin :pin)}])
   (when-not (or deleted? deleted-for-me?)
     [{:type     :danger
       :on-press (fn []
                   (rf/dispatch
                    [:hide-bottom-sheet])
                   (rf/dispatch [:chat.ui/delete-message-for-me message-data
                                 constants/delete-message-for-me-undo-time-limit-ms]))

       :label    (i18n/label :t/delete-for-me)
       :icon     :i/delete
       :id       :delete-for-me}])
   (when (cond
           deleted?   false
           outgoing   true
           community? can-delete-message-for-everyone?
           group-chat group-admin?
           :else      false)
     [{:type     :danger
       :on-press (fn []
                   (rf/dispatch [:hide-bottom-sheet])
                   (rf/dispatch [:chat.ui/delete-message message-data
                                 constants/delete-message-undo-time-limit-ms]))
       :label    (i18n/label :t/delete-for-everyone)
       :icon     :i/delete
       :id       :delete-for-all}])))

(defn extract-id
  [reactions id]
  (->> reactions
       (filter (fn [{:keys [emoji-id]}] (= emoji-id id)))
       first
       :emoji-reaction-id))

(defn reactions
  [{:keys [chat-id message-id]}]
  (let [msg-reactions (rf/sub [:chats/message-reactions message-id chat-id])
        own-reactions (reduce (fn [acc {:keys [emoji-id own emoji-reaction-id]}]
                                (if own
                                  (assoc acc emoji-id emoji-reaction-id)
                                  acc))
                              {}
                              msg-reactions)]
    [rn/view
     {:style {:flex-direction     :row
              :justify-content    :space-between
              :padding-horizontal 30
              :padding-top        5
              :padding-bottom     15}}
     (for [[id reaction-name] constants/reactions
           :let               [emoji-reaction-id (get own-reactions id)]]
       ^{:key id}
       [quo/reactions reaction-name
        {:start-pressed? (boolean emoji-reaction-id)
         :accessibility-label (str "reaction-" (name reaction-name))
         :on-press
         (fn []
           (if emoji-reaction-id
             (rf/dispatch [:models.reactions/send-emoji-reaction-retraction
                           {:message-id        message-id
                            :emoji-id          id
                            :emoji-reaction-id emoji-reaction-id}])
             (rf/dispatch [:models.reactions/send-emoji-reaction
                           {:message-id message-id
                            :emoji-id   id}]))
           (rf/dispatch [:hide-bottom-sheet]))}])]))

(defn reactions-and-actions
  [message-data
   {:keys [chat-id] :as context}]
  (fn []
    (let [data                                          (if (contains? message-data :album-id)
                                                          (first (:album message-data))
                                                          message-data)
          {:keys [message-id deleted? deleted-for-me?]} data
          outgoing-status                               (:outgoing-status data)
          actions                                       (get-actions data context)
          main-actions                                  (filter #(= (:type %) :main) actions)
          danger-actions                                (filter #(= (:type %) :danger) actions)
          admin-actions                                 (filter #(= (:type %) :admin) actions)]
      [:<>
       ;; REACTIONS
       (when (and (not= outgoing-status :sending) (not (or deleted? deleted-for-me?)))
         [reactions {:chat-id chat-id :message-id message-id}])
       ;; MAIN ACTIONS
       [rn/view {:style {:padding-horizontal 8}}
        (for [action main-actions]
          (let [on-press (:on-press action)]
            ^{:key (:id action)}
            [quo/menu-item
             {:type                :main
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            (fn []
                                     (rf/dispatch [:hide-bottom-sheet])
                                     (when on-press (on-press)))}]))]
       
       (when-not (empty? danger-actions)
         [quo/separator])

       ;; DANGER ACTIONS
       [rn/view {:style {:padding-horizontal 8}}
        (for [action danger-actions]
          (let [on-press (:on-press action)]
            ^{:key (:id action)}
            [quo/menu-item
             {:type                :danger
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            (fn []
                                     (rf/dispatch [:hide-bottom-sheet])
                                     (when on-press (on-press)))}]))]
       
       (when-not (empty? admin-actions)
         [quo/separator])

       ;; ADMIN ACTIONS
       [rn/view {:style {:padding-horizontal 8}}
        (for [action admin-actions]
          (let [on-press (:on-press action)]
            ^{:key (:id action)}
            [quo/menu-item
             {:type                :danger
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            (fn []
                                     (rf/dispatch [:hide-bottom-sheet])
                                     (when on-press (on-press)))}]))]])))
