(ns status-im.ui.screens.group.core
  (:require [status-im.data-store.chats :as chats-store]
            [status-im.utils.fx :as fx]))

(fx/defn participants-added
  [{:keys [db] :as cofx} chat-id added-participants-set]
  (when (seq added-participants-set)
    {:db            (update-in db [:chats chat-id :contacts]
                               concat added-participants-set)
     :data-store/tx [(chats-store/add-chat-contacts-tx
                      chat-id added-participants-set)]}))

(fx/defn participants-removed
  [{:keys [now db] :as cofx} chat-id removed-participants-set]
  (when (seq removed-participants-set)
    (let [{:keys [is-active timestamp]} (get-in db [:chats chat-id])]
      ;;TODO: not sure what this condition is for
      (when (and is-active (>= now timestamp))
        {:db            (update-in db [:chats chat-id :contacts]
                                   (partial remove removed-participants-set))
         :data-store/tx [(chats-store/remove-chat-contacts-tx
                          chat-id removed-participants-set)]}))))
