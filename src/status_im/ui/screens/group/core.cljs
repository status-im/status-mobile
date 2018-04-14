(ns status-im.ui.screens.group.core)

(defn participants-added [chat-id added-participants-set {:keys [db] :as cofx}]
  (when (seq added-participants-set)
    {:db (update-in db [:chats chat-id :contacts] concat added-participants-set)
     :data-store/add-chat-contacts [chat-id added-participants-set]}))

(defn participants-removed [chat-id removed-participants-set {:keys [now db] :as cofx}]
  (when (seq removed-participants-set)
    (let [{:keys [is-active timestamp]} (get-in db [:chats chat-id])]
      ;;TODO: not sure what this condition is for
      (when (and is-active (>= now timestamp))
        {:db (update-in db [:chats chat-id :contacts] (partial remove removed-participants-set))
         :data-store/remove-chat-contacts [chat-id removed-participants-set]}))))
