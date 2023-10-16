(ns status-im2.contexts.chat.messages.content.reactions.view
  (:require
    [quo2.core :as quo]
    [quo2.theme :as quo.theme]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.messages.drawers.view :as drawers]
    [utils.re-frame :as rf]))

(defn- on-press
  [{:keys [own message-id emoji-id emoji-reaction-id]}]
  (if own
    (rf/dispatch [:models.reactions/send-emoji-reaction-retraction
                  {:message-id        message-id
                   :emoji-id          emoji-id
                   :emoji-reaction-id emoji-reaction-id}])
    (rf/dispatch [:models.reactions/send-emoji-reaction
                  {:message-id message-id
                   :emoji-id   emoji-id}])))

(defn- on-long-press
  [{:keys [message-id emoji-id user-message-content reactions-order theme]}]
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
                                              {:reactions-order reactions-order
                                               :theme           theme}])
                                  :selected-item (fn []
                                                   user-message-content)
                                  :padding-bottom-override 0}]))}]))

(defn- on-press-add
  [{:keys [chat-id message-id user-message-content]}]
  (rf/dispatch [:dismiss-keyboard])
  (rf/dispatch
   [:show-bottom-sheet
    {:content       (fn [] [drawers/reactions
                            {:chat-id    chat-id
                             :message-id message-id}])
     :selected-item (fn []
                      user-message-content)}]))

(defn- add-emoji-key
  [reaction]
  (assoc reaction
         :emoji
         (get constants/reactions (:emoji-id reaction))))

(defn- view-internal
  [{:keys [message-id chat-id pinned-by theme]} user-message-content]
  (let [reactions (rf/sub [:chats/message-reactions message-id chat-id])]
    [:<>
     (when (seq reactions)
       [quo/react
        {:container-style {:margin-left 44
                           :margin-top  8}
         :reactions       (map add-emoji-key reactions)
         :add-reaction?   true
         :use-case        (when pinned-by :pinned)
         :on-press        #(on-press (assoc % :message-id message-id))
         :on-long-press   #(on-long-press (assoc %
                                                 :message-id      message-id
                                                 :theme           theme
                                                 :reactions-order (map :emoji-id reactions)))
         :on-press-add    #(on-press-add {:chat-id              chat-id
                                          :message-id           message-id
                                          :user-message-content user-message-content})}])]))

(def message-reactions-row (quo.theme/with-theme view-internal))
