(ns status-im.contexts.chat.messenger.messages.content.reactions.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.messages.drawers.view :as drawers]
    [utils.re-frame :as rf]))

(defn- on-press
  [{:keys [own message-id emoji-id emoji-reaction-id]}]
  (if own
    (rf/dispatch [:reactions/send-emoji-reaction-retraction emoji-reaction-id])
    (rf/dispatch [:reactions/send-emoji-reaction
                  {:message-id message-id
                   :emoji-id   emoji-id}])))

(defn- on-long-press
  [{:keys [message-id emoji-id user-message-content-render-fn reactions-order theme]}]
  (rf/dispatch
   [:reactions/get-authors-by-message-id
    {:message-id message-id
     :on-success (fn [response]
                   (rf/dispatch [:reactions/save-authors
                                 {:reaction-authors-list response
                                  :selected-reaction     emoji-id}])
                   (rf/dispatch [:dismiss-keyboard])
                   (rf/dispatch [:show-bottom-sheet
                                 {:on-close                #(rf/dispatch
                                                             [:reactions/clear-authors])
                                  :content                 (fn []
                                                             [drawers/reaction-authors
                                                              {:reactions-order reactions-order
                                                               :theme           theme}])
                                  :selected-item           (fn [] [user-message-content-render-fn
                                                                   {:hide-reactions? true}])
                                  :padding-bottom-override 0}]))}]))

(defn- on-press-add
  [{:keys [chat-id message-id user-message-content-render-fn]}]
  (rf/dispatch [:dismiss-keyboard])
  (rf/dispatch
   [:show-bottom-sheet
    {:content       (fn [] [drawers/reactions
                            {:chat-id    chat-id
                             :message-id message-id}])
     :selected-item (fn [] [user-message-content-render-fn])}]))

(defn- add-emoji-key
  [reaction]
  (assoc reaction
         :emoji
         (get constants/reactions (:emoji-id reaction))))

(defn message-reactions-row
  [{:keys [message-id chat-id pinned-by hide-new-reaction-button?]} user-message-content-render-fn]
  (let [theme     (quo.theme/use-theme)
        reactions (rf/sub [:chats/message-reactions message-id chat-id])]
    (when (seq reactions)
      [quo/react
       {:container-style           {:margin-left 44
                                    :margin-top  8}
        :reactions                 (map add-emoji-key reactions)
        :hide-new-reaction-button? hide-new-reaction-button?
        :use-case                  (when pinned-by :pinned)
        :on-press                  #(on-press (assoc % :message-id message-id))
        :on-long-press             #(on-long-press (assoc %
                                                          :message-id message-id
                                                          :theme theme
                                                          :reactions-order (map :emoji-id
                                                                                reactions)
                                                          :user-message-content-render-fn
                                                          user-message-content-render-fn))
        :on-press-add              #(on-press-add {:chat-id chat-id
                                                   :message-id message-id
                                                   :user-message-content-render-fn
                                                   user-message-content-render-fn})}])))
