(ns status-im2.contexts.chat.messages.content.reactions.view
  (:require [status-im2.constants :as constants]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.messages.drawers.view :as drawers]
            [quo2.theme :as quo.theme]))

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
  [{:keys [message-id emoji-id user-message-content reactions theme]}]
  (rf/dispatch
   [:chat.ui/emoji-reactions-by-message-id
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
                                             [drawers/reaction-authors
                                              {:reactions-order reactions
                                               :theme           theme}])
                                  :selected-item (fn []
                                                   user-message-content)
                                  :padding-bottom-override 0}]))}]))

(defn- view-internal
  [{:keys [message-id chat-id theme]} user-message-content]
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
             :on-long-press       #(on-long-press
                                    {:message-id           message-id
                                     :emoji-id             emoji-id
                                     :user-message-content user-message-content
                                     :reactions            (map :emoji-id reactions)
                                     :theme                theme})
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

(def message-reactions-row (quo.theme/with-theme view-internal))
