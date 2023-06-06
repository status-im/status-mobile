(ns status-im2.contexts.chat.messages.content.reactions.view
  (:require [status-im2.constants :as constants]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.messages.drawers.view :as drawers]))

(defn- on-press
  [own message-id emoji-id emoji-reaction-id]
  (if own
    (rf/dispatch [:models.reactions/send-emoji-reaction-retraction
                  {:message-id        message-id
                   :emoji-id          emoji-id
                   :emoji-reaction-id emoji-reaction-id}])
    (rf/dispatch [:models.reactions/send-emoji-reaction
                  {:message-id message-id
                   :emoji-id   emoji-id}])))

(defn- on-long-press
  [message-id emoji-id user-message-content reactions]
  (rf/dispatch [:chat.ui/emoji-reactions-by-message-id
                {:message-id message-id
                 :on-success (fn [response]
                               (rf/dispatch [:chat/save-emoji-reaction-details
                                             {:reaction-authors-list response
                                              :selected-reaction     emoji-id}])
                               (rf/dispatch [:dismiss-keyboard])
                               (rf/dispatch [:show-bottom-sheet
                                             {:on-close (fn []
                                                          (rf/dispatch
                                                           [:chat/clear-emoji-reaction-author-details]))
                                              :content (fn []
                                                         [drawers/reaction-authors reactions])
                                              :selected-item (fn []
                                                               user-message-content)
                                              :padding-bottom-override 0}]))}]))

(defn message-reactions-row
  [{:keys [message-id chat-id]} user-message-content]
  (let [reactions (rf/sub [:chats/message-reactions message-id chat-id])]
    [:<>
     (when (seq reactions)
       [rn/scroll-view
        {:shows-horizontal-scroll-indicator false
         :horizontal                        true
         :style                             {:margin-left    44
                                             :margin-top     8
                                             :flex-direction :row}}
        (for [{:keys [own emoji-id quantity emoji-reaction-id]
               :as   emoji-reaction} reactions]
          ^{:key emoji-reaction}
          [rn/view {:style {:margin-right 6}}
           [quo/reaction
            {:emoji               (get constants/reactions emoji-id)
             :neutral?            own
             :clicks              quantity
             :on-press            #(on-press own message-id emoji-id emoji-reaction-id)
             :on-long-press       #(on-long-press message-id
                                                  emoji-id
                                                  user-message-content
                                                  (map :emoji-id reactions))
             :accessibility-label (str "emoji-reaction-" emoji-id)}]])
        [quo/add-reaction
         {:on-press (fn []
                      (rf/dispatch [:dismiss-keyboard])
                      (rf/dispatch
                       [:show-bottom-sheet
                        {:content       (fn [] [drawers/reactions
                                                {:chat-id    chat-id
                                                 :message-id message-id}])
                         :selected-item (fn []
                                          user-message-content)}]))}]])]))
