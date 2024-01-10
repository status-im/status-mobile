(ns status-im.contexts.chat.messenger.messages.content.lightbox.view
  (:require
    [oops.core :as oops]
    [reagent.core :as reagent]
    [status-im.common.lightbox.constants :as constants]
    [status-im.contexts.chat.messenger.messages.content.lightbox.style :as style]
    [status-im.contexts.chat.messenger.messages.content.text.view :as message-view]))

(defn bottom-text-for-lightbox
  [_]
  (let [text-height (reagent/atom 0)]
    (fn [{:keys [content chat-id] :as _message}]
      (let [expandable-text? (> @text-height (* constants/line-height 2))]
        [message-view/render-parsed-text
         {:content        content
          :chat-id        chat-id
          :style-override (style/bottom-text expandable-text?)
          :on-layout      #(reset! text-height (oops/oget % "nativeEvent.layout.height"))}]))))
