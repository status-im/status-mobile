(ns status-im2.contexts.chat.messages.content.link-preview.view
  (:require
    [quo2.core :as quo]
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
  (let [previews (rf/sub [:chats/message-link-previews chat-id message-id])]
    (when (seq previews)
      [:<>
       (for [{:keys [url title description thumbnail hostname]} previews]
         ^{:key url}
         [quo/link-preview
          {:title           title
           :description     description
           :link            hostname
           :thumbnail       (:url thumbnail)
           :thumbnail-size  (when (nearly-square? thumbnail) :large)
           :container-style {:margin-top 8}}])])))
