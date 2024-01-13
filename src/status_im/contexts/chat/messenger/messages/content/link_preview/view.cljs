(ns status-im.contexts.chat.messenger.messages.content.link-preview.view
  (:require
    [quo.core :as quo]
    [utils.re-frame :as rf]))

(defn nearly-square?
  [{:keys [width height]}]
  (if (or (zero? width) (zero? height))
    false
    (let [ratio (/ (max width height)
                   (min width height))]
      (< (Math/abs (dec ratio)) 0.1))))

(defn view
  [{:keys [chat-id message-id]}]
  (let [previews             (rf/sub [:chats/message-link-previews chat-id message-id])
        status-link-previews (rf/sub [:chats/message-status-link-previews chat-id message-id])]
    (when (or (seq status-link-previews)
              (seq previews))
      [:<>
       (for [{:keys [url title description thumbnail hostname community]}
             (concat status-link-previews previews)]
         ^{:key url}
         (if community
           (let [{community-description :description
                  community-icon        :icon
                  community-banner      :banner
                  community-name        :display-name
                  members-count         :members-count} community]
             [quo/internal-link-card
              {:type          :community
               :size          :message
               :description   community-description
               :members-count members-count
               :title         community-name
               :banner        (:url community-banner)
               :icon          (:url community-icon)
               :on-press      #(rf/dispatch [:universal-links/handle-url url])}])
           [quo/link-preview
            {:title           title
             :description     description
             :link            hostname
             :thumbnail       (:url thumbnail)
             :thumbnail-size  (when (nearly-square? thumbnail) :large)
             :container-style {:margin-top 8}}]))])))
