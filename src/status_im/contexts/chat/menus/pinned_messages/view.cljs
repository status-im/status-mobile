(ns status-im.contexts.chat.menus.pinned-messages.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [react-native.gesture :as gesture]
    [status-im.contexts.chat.menus.pinned-messages.style :as style]
    [status-im.contexts.chat.messages.content.view :as message]
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
  [{:keys [community?]}]
  [rn/view {:style style/no-pinned-messages-container}
   [rn/view {:style style/no-pinned-messages-icon}
    [quo/icon :i/placeholder]]
   [rn/view {:style style/no-pinned-messages-content}
    [quo/text
     {:size   :paragraph-1
      :weight :semi-bold
      :style  style/no-pinned-messages-title}
     (i18n/label :t/no-pinned-messages)]
    [quo/text
     {:size  :paragraph-2
      :style style/no-pinned-messages-text}
     (i18n/label
      (if community?
        :t/no-pinned-messages-community-desc
        :t/no-pinned-messages-desc))]]])

(defn pinned-messages
  [chat-id]
  (let [pinned                 (rf/sub [:chats/pinned-sorted-list chat-id])
        render-data            (rf/sub [:chats/current-chat-message-list-view-context :in-pinned-view])
        current-chat           (rf/sub [:chat-by-id chat-id])
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
         :footer      [rn/view {:style style/list-footer}]
         :key-fn      list-key-fn
         :separator   [quo/separator {:style {:margin-vertical 8}}]}]
       [empty-pinned-messages-state
        {:community? (boolean community)}])]))
