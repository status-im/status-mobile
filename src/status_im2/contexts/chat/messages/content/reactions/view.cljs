(ns status-im2.contexts.chat.messages.content.reactions.view
  (:require [status-im2.constants :as constants]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.messages.drawers.view :as drawers]))

(defn message-reactions-row
  [chat-id message-id]
  (let [reactions (rf/sub [:chats/message-reactions message-id chat-id])]
    (when (seq reactions)
      [rn/view {:margin-left 52 :margin-bottom 12 :flex-direction :row}
       (for [{:keys [own emoji-id quantity emoji-reaction-id] :as emoji-reaction} reactions]
         ^{:key (str emoji-reaction)}
         [rn/view {:style {:margin-right 6}}
          [quo/reaction
           {:emoji               (get constants/reactions emoji-id)
            :neutral?            own
            :clicks              quantity
            :on-press            (if own
                                   #(rf/dispatch [:models.reactions/send-emoji-reaction-retraction
                                                  {:message-id        message-id
                                                   :emoji-id          emoji-id
                                                   :emoji-reaction-id emoji-reaction-id}])
                                   #(rf/dispatch [:models.reactions/send-emoji-reaction
                                                  {:message-id message-id
                                                   :emoji-id   emoji-id}]))
            :accessibility-label (str "emoji-reaction-" emoji-id)}]])
       [quo/add-reaction
        {:on-press #(rf/dispatch [:bottom-sheet/show-sheet
                                  {:content (fn [] [drawers/reactions chat-id message-id])}])}]])))
