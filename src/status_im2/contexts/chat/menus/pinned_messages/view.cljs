(ns status-im2.contexts.chat.menus.pinned-messages.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.chat.messages.content.deleted.view :as content.deleted]
            [status-im2.contexts.chat.messages.content.view :as message]
            [status-im2.contexts.chat.menus.pinned-messages.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [react-native.gesture :as gesture]
            [react-native.fast-image :as fast-image]))

(def list-key-fn #(or (:message-id %) (:value %)))

(defn community-avatar
  [community-images]
  (when community-images
    (:uri (or (:thumbnail community-images)
              (:large community-images)
              (first community-images)))))

(defn message-render-fn
  [{:keys [deleted? deleted-for-me?] :as message} _ _ context]
  (if (or deleted? deleted-for-me?)
    [content.deleted/deleted-message message context]
    [message/message message context (atom false)]))

(defn pinned-messages
  [chat-id]
  (let [pinned                 (rf/sub [:chats/pinned-sorted-list chat-id])
        render-data            (rf/sub [:chats/current-chat-message-list-view-context :in-pinned-view])
        current-chat           (rf/sub [:chat-by-id chat-id])
        {:keys [community-id]} current-chat
        community              (rf/sub [:communities/community community-id])
        bottom-inset           (safe-area/get-bottom)
        community-images       (rf/sub [:community/images community-id])]
    [gesture/scroll-view
     {:accessibility-label :pinned-messages-menu}
     [:<>
      [quo/text
       {:size   :heading-2
        :weight :semi-bold
        :style  (style/heading community)}
       (i18n/label :t/pinned-messages)]
      (when community
        [rn/view {:style (style/heading-container)}
         [fast-image/fast-image
          {:source (community-avatar community-images)
           :style  {:width         20
                    :height        20
                    :border-radius 20}}]
         [rn/text {:style (style/heading-text)} (:name community)]
         [quo/icon
          :i/chevron-right
          {:color (colors/theme-colors colors/neutral-60 colors/neutral-30)
           :size  12}]
         [rn/text
          {:style (style/chat-name-text)}
          (str "# " (:chat-name current-chat))]])]
     (if (pos? (count pinned))
       [rn/flat-list
        {:data        pinned
         :render-data render-data
         :render-fn   message-render-fn
         :footer      [rn/view {:style (style/list-footer bottom-inset)}]
         :key-fn      list-key-fn
         :separator   [quo/separator {:style {:margin-vertical 8}}]}]
       [rn/view {:style (style/no-pinned-messages-container bottom-inset)}
        [rn/view {:style style/no-pinned-messages-icon}
         [quo/icon :i/placeholder]]
        [quo/text
         {:weight :semi-bold
          :style  style/no-pinned-messages-text}
         (i18n/label :t/no-pinned-messages)]
        [quo/text {:size :paragraph-2}
         (i18n/label
          (if community
            :t/no-pinned-messages-community-desc
            :t/no-pinned-messages-desc))]])]))
