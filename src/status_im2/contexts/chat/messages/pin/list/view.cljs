(ns status-im2.contexts.chat.messages.pin.list.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.chat.messages.content.view :as message]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def list-key-fn #(or (:message-id %) (:value %)))

(defn get-render-data
  [{:keys [group-chat chat-id public? community-id admins space-keeper edit-enabled
           in-pinned-view?]}]
  (let [current-public-key (rf/sub [:multiaccount/public-key])
        {:keys [can-delete-message-for-everyone-in-community?] :as community} (rf/sub
                                                                               [:communities/community
                                                                                community-id])
        group-admin? (get admins current-public-key)
        community-admin? (when community (community :admin))
        message-pin-enabled (and (not public?)
                                 (or (not group-chat)
                                     (and group-chat
                                          (or group-admin?
                                              community-admin?))))]
    {:group-chat                                    group-chat
     :public?                                       public?
     :community?                                    (not (nil? community-id))
     :group-admin?                                  group-admin?
     :community-admin?                              community-admin?
     :current-public-key                            current-public-key
     :space-keeper                                  space-keeper
     :chat-id                                       chat-id
     :message-pin-enabled                           message-pin-enabled
     :edit-enabled                                  edit-enabled
     :in-pinned-view?                               in-pinned-view?
     :can-delete-message-for-everyone-in-community? can-delete-message-for-everyone-in-community?}))

(defn message-render-fn
  [message _ _ context]
  ;; TODO (flexsurfer) probably we don't want reactions here
  [message/message-with-reactions message context])

(defn pinned-messages-list
  [chat-id]
  (let [pinned-messages                                          (vec (vals (rf/sub [:chats/pinned
                                                                                     chat-id])))
        current-chat                                             (rf/sub [:chat-by-id chat-id])

        {:keys [group-chat chat-id public? community-id admins]}
        current-chat

        community                                                (rf/sub [:communities/community
                                                                          community-id])]
    [rn/view {:accessibility-label :pinned-messages-list}
     ;; TODO (flexsurfer) this should be a component in quo2
     ;; https://github.com/status-im/status-mobile/issues/14529
     [:<>
      [quo/text
       {:size   :heading-1
        :weight :semi-bold
        :style  {:margin-horizontal 20}}
       (i18n/label :t/pinned-messages)]
      (when community
        [rn/view
         {:style {:flex-direction    :row
                  :background-color  (colors/theme-colors colors/neutral-10 colors/neutral-80)
                  :border-radius     20
                  :align-items       :center
                  :align-self        :flex-start
                  :margin-horizontal 20
                  :padding           4
                  :margin-top        8}}
         [rn/text {:style {:margin-left 6 :margin-right 4}} (:name community)]
         [quo/icon
          :i/chevron-right
          {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)
           :size  12}]
         [rn/text
          {:style {:margin-left  4
                   :margin-right 8}}
          (str "# " (:chat-name current-chat))]])]
     (if (> (count pinned-messages) 0)
       [rn/flat-list
        {:data        pinned-messages
         :render-data (get-render-data {:group-chat      group-chat
                                        :chat-id         chat-id
                                        :public?         public?
                                        :community-id    community-id
                                        :admins          admins
                                        :edit-enabled    true
                                        :in-pinned-view? false})
         :render-fn   message-render-fn
         :key-fn      list-key-fn
         :separator   quo/separator}]
       [rn/view
        {:style {:justify-content :center
                 :align-items     :center
                 :margin-top      20}}
        [rn/view
         {:style {:width           120
                  :height          120
                  :justify-content :center
                  :align-items     :center
                  :border-width    1}} [quo/icon :i/placeholder]]
        [quo/text
         {:weight :semi-bold
          :style  {:margin-top 20}}
         (i18n/label :t/no-pinned-messages)]
        [quo/text {:size :paragraph-2}
         (i18n/label
          (if community :t/no-pinned-messages-community-desc :t/no-pinned-messages-desc))]])]))
