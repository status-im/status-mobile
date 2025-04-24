(ns status-im.contexts.chat.messenger.menus.pinned-messages.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.common.resources :as resources]
    [status-im.contexts.chat.messenger.menus.pinned-messages.style :as style]
    [status-im.contexts.chat.messenger.messages.content.view :as message]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def list-key-fn #(or (:message-id %) (:value %)))

(defn community-avatar
  [community-images]
  (when community-images
    (:uri (or (:thumbnail community-images)
              (:large community-images)
              (first community-images)))))

(defn message-render-fn
  [message _ _ context]
  [message/message message context (atom false)])

(defn empty-pinned-messages-state
  []
  (let [theme (quo.context/use-theme)]
    [rn/view {:style style/no-pinned-messages-container}
     [quo/empty-state
      {:blur?       false
       :image       (resources/get-themed-image :no-pinned-messages theme)
       :title       (i18n/label :t/no-pinned-messages)
       :description (i18n/label :t/no-pinned-messages-desc)}]]))

(defn view
  [{:keys [chat-id]}]
  (let [pinned                 (rf/sub [:chats/pinned-sorted-list chat-id])
        screen-id              (quo.context/use-screen-id)
        render-data            (rf/sub [:chats/current-chat-message-list-view-context :in-pinned-view])
        current-chat           (rf/sub [:chats/chat-by-id chat-id])
        {:keys [community-id]} current-chat
        community              (rf/sub [:communities/community community-id])
        community-images       (rf/sub [:community/images community-id])]
    [gesture/scroll-view
     {:accessibility-label :pinned-messages-menu
      :bounces             false}
     [:<>
      [quo/text
       {:size   :heading-2
        :weight :semi-bold
        :style  (style/heading community)}
       (i18n/label :t/pinned-messages)]
      (when community
        [quo/context-tag
         {:type            :channel
          :size            24
          :container-style style/community-tag-container
          :community-logo  (community-avatar community-images)
          :community-name  (:name community)
          :channel-name    (:chat-name current-chat)}])]
     (if (pos? (count pinned))
       [rn/flat-list
        {:data        pinned
         :render-data (assoc render-data :disable-message-long-press? (not= screen-id :screen/chat))
         :render-fn   message-render-fn
         :footer      [rn/view {:style style/list-footer}]
         :key-fn      list-key-fn
         :separator   [quo/separator {:style {:margin-vertical 8}}]}]
       [empty-pinned-messages-state])]))
