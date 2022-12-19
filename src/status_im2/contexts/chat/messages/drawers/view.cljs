(ns status-im2.contexts.chat.messages.drawers.view
  (:require [react-native.core :as rn]
            [status-im2.common.constants :as constants]
            [utils.re-frame :as rf]
            [quo2.core :as quo]
            [i18n.i18n :as i18n]
            [status-im2.setup.config :as config]

            ;; TODO (flexsurfer) refactor and move to status-im2
            [status-im.ui.components.react :as react]
            [status-im.ui2.screens.chat.components.reply :as components.reply]))

(defn pin-message [{:keys [chat-id pinned] :as message-data}]
  ;;TODO (flexsurfer) move this to one event (get data from db not from sub)
  (let [pinned-messages (rf/sub [:chats/pinned chat-id])]
    (if (and (not pinned) (> (count pinned-messages) 2))
      (do
        (js/setTimeout (fn [] (rf/dispatch [:dismiss-keyboard])) 500)
        (rf/dispatch [:pin-message/show-pin-limit-modal chat-id]))
      (rf/dispatch [:pin-message/send-pin-message (assoc message-data :pinned (not pinned))]))))

(defn get-actions [{:keys [outgoing edit-enabled show-input? content message-pin-enabled pinned community? can-delete-message-for-everyone?] :as message-data}]
  (concat
   (when (and outgoing edit-enabled)
     [{:type     :main
       :on-press #(rf/dispatch [:chat.ui/edit-message message-data])
       :label    (i18n/label :t/edit-message)
       :icon     :i/edit
       :id       :edit}])
   (when show-input?
     [{:type     :main
       :on-press #(rf/dispatch [:chat.ui/reply-to-message message-data])
       :label    (i18n/label :t/message-reply)
       :icon     :i/reply
       :id       :reply}])
   [{:type     :main
     ;; TODO (flexsurfer) move this fo fx (get data from db not from sub)
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
       :on-press #(rf/dispatch [:chat.ui/delete-message-for-me message-data constants/delete-message-for-me-undo-time-limit-ms])
       :label    (i18n/label :t/delete-for-me)
       :icon     :i/delete
       :id       :delete-for-me}])
   (when (and (or outgoing can-delete-message-for-everyone?) config/delete-message-enabled?)
     [{:type     :danger
       :on-press #(rf/dispatch [:chat.ui/delete-message message-data constants/delete-message-undo-time-limit-ms])
       :label    (i18n/label :t/delete-for-everyone)
       :icon     :i/delete
       :id       :delete-for-all}])))

(defn reactions [message-id]
  [rn/view {:style {:flex-direction     :row
                    :justify-content    :space-between
                    :padding-horizontal 30
                    :padding-top        5
                    :padding-bottom     15}}
   (doall
    (for [[id icon] constants/reactions]
      ;:let [active (own-reactions id)]]
      ;;TODO reactions selector should be used https://www.figma.com/file/WQZcp6S0EnzxdTL4taoKDv/Design-System?node-id=9961%3A166549
      ;; not implemented yet
      ^{:key id}
      [quo/button (merge
                   {:size                40
                    :type                :grey
                    :icon                true
                    :icon-no-color       true
                    :accessibility-label (str "emoji-picker-" id)
                    :on-press            #(do
                                            (rf/dispatch [:models.reactions/send-emoji-reaction
                                                          {:message-id message-id
                                                           :emoji-id   id}])
                                            (rf/dispatch [:bottom-sheet/hide]))})
       ;(when active {:style {:background-color colors/neutral-10}}))
       icon]))])

;; TODO (flexsurfer) this probably should be a component in quo2 https://www.figma.com/file/WQZcp6S0EnzxdTL4taoKDv/Design-System?node-id=5626%3A158317&t=1JiRhBswpVXZ4SYF-3
(defn reactions-and-actions [message-data]
  (fn []
    (let [actions (get-actions message-data)
          main-actions (filter #(= (:type %) :main) actions)
          danger-actions (filter #(= (:type %) :danger) actions)
          admin-actions (filter #(= (:type %) :admin) actions)]
      [:<>
       ;; REACTIONS
       [reactions (:message-id message-data)]

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
              :on-press            #(do
                                      (when on-press (on-press))
                                      (rf/dispatch [:bottom-sheet/hide]))}]))
        (when-not (empty? danger-actions)
          [quo/separator])

        ;; DANAGER ACTIONS
        (for [action danger-actions]
          (let [on-press (:on-press action)]
            ^{:key (:id action)}
            [quo/menu-item
             {:type                :danger
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            #(do
                                      (when on-press (on-press))
                                      (rf/dispatch [:bottom-sheet/hide]))}]))
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
              :on-press            #(do
                                      (when on-press (on-press))
                                      (rf/dispatch [:bottom-sheet/hide]))}]))]])))
