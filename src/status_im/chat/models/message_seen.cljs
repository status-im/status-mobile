(ns status-im.chat.models.message-seen
  (:require [status-im.utils.fx :as fx]
            [status-im.data-store.messages :as messages-store]
            [status-im.utils.platform :as platform]))

(defn- unread-messages-number [chats]
  (apply + (map :unviewed-messages-count chats)))

(fx/defn update-dock-badge-label
  [cofx]
  (let [chats                      (get-in cofx [:db :chats])
        active-chats               (filter :is-active (vals chats))
        private-chats              (filter (complement :public?) active-chats)
        public-chats               (filter :public? active-chats)
        private-chats-unread-count (unread-messages-number private-chats)
        public-chats-unread-count  (unread-messages-number public-chats)
        label                      (cond
                                     (pos? private-chats-unread-count) private-chats-unread-count
                                     (pos? public-chats-unread-count) "•"
                                     :else nil)]
    {:set-dock-badge-label label}))

(defn subtract-seen-messages
  [old-count new-seen-messages-ids]
  (max 0 (- old-count (count new-seen-messages-ids))))

(fx/defn update-chats-unviewed-messages-count
  [{:keys [db] :as cofx} {:keys [chat-id loaded-unviewed-messages-ids]}]
  (let [{:keys [loaded-unviewed-messages-ids unviewed-messages-count]}
        (get-in db [:chats chat-id])]
    {:db (update-in db [:chats chat-id] assoc
                    :unviewed-messages-count (subtract-seen-messages
                                              unviewed-messages-count
                                              loaded-unviewed-messages-ids)
                    :loaded-unviewed-messages-ids #{})}))

(fx/defn mark-messages-seen
  "Marks all unviewed loaded messages as seen in particular chat"
  [{:keys [db] :as cofx} chat-id]
  (let [loaded-unviewed-ids (get-in db [:chats chat-id :loaded-unviewed-messages-ids])]
    (when (seq loaded-unviewed-ids)
      (fx/merge cofx
                {:db (reduce (fn [acc message-id]
                               (assoc-in acc [:messages chat-id message-id :seen]
                                         true))
                             db
                             loaded-unviewed-ids)}
                (messages-store/mark-messages-seen chat-id loaded-unviewed-ids nil)
                (update-chats-unviewed-messages-count {:chat-id chat-id})
                (when platform/desktop?
                  (update-dock-badge-label))))))
