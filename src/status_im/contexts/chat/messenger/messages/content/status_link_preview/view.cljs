(ns status-im.contexts.chat.messenger.messages.content.status-link-preview.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [chat-id message-id]}]
  (let [status-link-previews (rf/sub [:chats/message-status-link-previews chat-id message-id])
        link-previews?       (rf/sub [:chats/message-link-previews? chat-id message-id])]
    (when (seq status-link-previews)
      [rn/view {:style {:margin-top (when link-previews? 8)}}
       (for [{:keys [url community]} status-link-previews]
         (when (:display-name community)
           (let [{community-description :description
                  community-icon        :icon
                  community-banner      :banner
                  community-name        :display-name
                  members-count         :members-count} community]
             ^{:key url}
             [quo/internal-link-card
              {:type          :community
               :size          :message
               :description   community-description
               :members-count members-count
               :title         community-name
               :banner        (:url community-banner)
               :icon          (:url community-icon)
               :on-press      #(rf/dispatch [:universal-links/handle-url url])}])))])))
