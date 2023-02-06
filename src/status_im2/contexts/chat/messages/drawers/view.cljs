(ns status-im2.contexts.chat.messages.drawers.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im.ui.components.react :as react]
            [status-im.ui2.screens.chat.components.reply.view :as components.reply]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.config :as config]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

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
  [{:keys [outgoing content pinned outgoing-status] :as message-data}
   {:keys [edit-enabled show-input? community? community-admin?
           can-delete-message-for-everyone?
           message-pin-enabled group-chat group-admin?]}]
  (concat
   (when (and outgoing edit-enabled)
     [{:type     :main
       :on-press #(rf/dispatch [:chat.ui/edit-message message-data])
       :label    (i18n/label :t/edit-message)
       :icon     :i/edit
       :id       :edit}])
   (when (and show-input? (not= outgoing-status :sending))
     [{:type     :main
       :on-press #(rf/dispatch [:chat.ui/reply-to-message message-data])
       :label    (i18n/label :t/message-reply)
       :icon     :i/reply
       :id       :reply}])
   [{:type     :main
     :on-press #(react/copy-to-clipboard
                 (components.reply/get-quoted-text-with-mentions
                  (get content :parsed-text)))
     :label    (i18n/label :t/copy-text)
     :icon     :i/copy
     :id       :copy}]
   (when message-pin-enabled
     [{:type     :main
       :on-press #(pin-message message-data)
       :label    (i18n/label (if pinned
                               (if community? :t/unpin-from-channel :t/unpin-from-chat)
                               (if community? :t/pin-to-channel :t/pin-to-chat)))
       :icon     :i/pin
       :id       (if pinned :unpin :pin)}])
   (when-not pinned
     [{:type     :danger
       :on-press (fn []
                   (rf/dispatch
                    [:bottom-sheet/hide])
                   (rf/dispatch [:chat.ui/delete-message-for-me message-data
                                 constants/delete-message-for-me-undo-time-limit-ms]))

       :label    (i18n/label :t/delete-for-me)
       :icon     :i/delete
       :id       :delete-for-me}])
   (when (and config/delete-message-enabled?
              (cond
                outgoing   true
                community? (or can-delete-message-for-everyone?
                               community-admin?)
                group-chat group-admin?
                :else      false))
     [{:type     :danger
       :on-press (fn []
                   (rf/dispatch [:bottom-sheet/hide])
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
  (let [reactions     (rf/sub [:chats/message-reactions message-id chat-id])
        own-reactions (reduce (fn [acc {:keys [emoji-id own emoji-reaction-id]}]
                                (if own
                                  (assoc acc emoji-id emoji-reaction-id)
                                  acc))
                              {}
                              reactions)]
    [rn/view
     {:style {:flex-direction     :row
              :justify-content    :space-between
              :padding-horizontal 30
              :padding-top        5
              :padding-bottom     15}}
     (doall
      (for [[id icon] constants/reactions]
        (let [emoji-reaction-id (get own-reactions id)]
          ^{:key id}
          [not-implemented/not-implemented
           [quo/button
            (merge
             {:size                40
              :type                (if emoji-reaction-id :grey :ghost)
              :icon                true
              :icon-no-color       true
              :accessibility-label (str "emoji-picker-" id)
              :on-press            (fn []
                                     (if emoji-reaction-id
                                       (rf/dispatch [:models.reactions/send-emoji-reaction-retraction
                                                     {:message-id        message-id
                                                      :emoji-id          id
                                                      :emoji-reaction-id emoji-reaction-id}])
                                       (rf/dispatch [:models.reactions/send-emoji-reaction
                                                     {:message-id message-id
                                                      :emoji-id   id}]))
                                     (rf/dispatch [:bottom-sheet/hide]))})
            icon]])))]))

(defn reactions-and-actions
  [{:keys [message-id outgoing-status] :as message-data} {:keys [chat-id] :as context}]
  (fn []
    (let [actions        (get-actions message-data context)
          main-actions   (filter #(= (:type %) :main) actions)
          danger-actions (filter #(= (:type %) :danger) actions)
          admin-actions  (filter #(= (:type %) :admin) actions)]
      [:<>
       ;; REACTIONS
       (when (not= outgoing-status :sending)
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
                                     (rf/dispatch [:bottom-sheet/hide])
                                     (when on-press (on-press)))}]))
        (when-not (empty? danger-actions)
          [quo/separator])

        ;; DANGER ACTIONS
        (for [action danger-actions]
          (let [on-press (:on-press action)]
            ^{:key (:id action)}
            [quo/menu-item
             {:type                :danger
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            (fn []
                                     (rf/dispatch [:bottom-sheet/hide])
                                     (when on-press (on-press)))}]))
        (when-not (empty? admin-actions)
          [quo/separator])

        ;; ADMIN ACTIONS
        (for [action admin-actions]
          (let [on-press (:on-press action)]
            ^{:key (:id action)}
            [quo/menu-item
             {:type                :danger
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            (fn []
                                     (rf/dispatch [:bottom-sheet/hide])
                                     (when on-press (on-press)))}]))]])))
