(ns status-im.chat.models.loading
  (:require [clojure.set :as set]
            [status-im.accounts.db :as accounts.db]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.models :as chat-model]
            [status-im.constants :as constants]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.utils.datetime :as time]
            [status-im.utils.fx :as fx]
            [status-im.utils.priority-map :refer [empty-message-map]]))

(def index-messages (partial into empty-message-map
                             (map (juxt :message-id identity))))

(defn- sort-references
  "Sorts message-references sequence primary by clock value,
  breaking ties by `:message-id`"
  [messages message-references]
  (sort-by (juxt (comp :clock-value (partial get messages) :message-id)
                 :message-id)
           message-references))

(fx/defn group-chat-messages
  "Takes chat-id, new messages + cofx and properly groups them
  into the `:message-groups`index in db"
  [{:keys [db]} chat-id messages]
  {:db (reduce (fn [db [datemark grouped-messages]]
                 (update-in db [:chats chat-id :message-groups datemark]
                            (fn [message-references]
                              (->> grouped-messages
                                   (map (fn [{:keys [message-id timestamp]}]
                                          {:message-id    message-id
                                           :timestamp-str (time/timestamp->time timestamp)}))
                                   (into (or message-references '()))
                                   (sort-references (get-in db [:chats chat-id :messages]))))))
               db
               (group-by (comp time/day-relative :timestamp) messages))})

(fx/defn group-messages
  [{:keys [db]}]
  (reduce-kv (fn [fx chat-id {:keys [messages]}]
               (group-chat-messages fx chat-id (vals messages)))
             {:db db}
             (:chats db)))

(defn- get-referenced-ids
  "Takes map of message-id->messages and returns set of message ids which are referenced by the original messages,
  excluding any message id, which is already in the original map"
  [message-id->messages]
  (into #{}
        (comp (keep (comp :response-to :content))
              (filter #(not (contains? message-id->messages %))))
        (vals message-id->messages)))

(fx/defn load-chats-messages
  [{:keys [db get-stored-messages get-stored-user-statuses
           get-referenced-messages get-stored-unviewed-messages]
    :as cofx}]
  (let [chats (:chats db)]
    (fx/merge
     cofx
     {:db (assoc
           db :chats
           (reduce
            (fn [chats chat-id]
              (let [stored-unviewed-messages (get-stored-unviewed-messages (accounts.db/current-public-key cofx))
                    chat-messages (index-messages (get-stored-messages chat-id))
                    message-ids   (keys chat-messages)]
                (update
                 chats
                 chat-id
                 assoc

                 :messages chat-messages
                 :message-statuses (get-stored-user-statuses chat-id message-ids)
                 :unviewed-messages (get stored-unviewed-messages chat-id)
                 :referenced-messages (into {}
                                            (map (juxt :message-id identity)
                                                 (get-referenced-messages
                                                  (get-referenced-ids chat-messages)))))))
            chats
            (keys chats)))}
     (group-messages))))

(fx/defn initialize-chats
  "Initialize all persisted chats on startup"
  [{:keys [db default-dapps all-stored-chats] :as cofx}]
  (let [chats (reduce (fn [acc {:keys [chat-id] :as chat}]
                        (assoc acc chat-id
                               (assoc chat :not-loaded-message-ids #{})))
                      {}
                      all-stored-chats)]
    (fx/merge cofx
              {:db (assoc db
                          :chats chats
                          :contacts/dapps default-dapps)}
              (commands/load-commands commands/register))))

(fx/defn initialize-pending-messages
  "Change status of own messages which are still in `sending` status to `not-sent`
  (If signal from status-go has not been received)"
  [{:keys [db] :as cofx}]
  (let [me               (accounts.db/current-public-key cofx)
        pending-statuses (->> (vals (:chats db))
                              (mapcat :message-statuses)
                              (mapcat (fn [[_ user-id->status]]
                                        (filter (comp (partial = :sending) :status)
                                                (get user-id->status me)))))
        updated-statuses (map #(assoc % :status :not-sent) pending-statuses)]
    {:data-store/tx [(user-statuses-store/save-statuses-tx updated-statuses)]
     :db            (reduce
                     (fn [acc {:keys [chat-id message-id status public-key]}]
                       (assoc-in acc
                                 [:chats chat-id :message-status message-id
                                  public-key :status]
                                 status))
                     db
                     updated-statuses)}))

(defn load-more-messages
  "Loads more messages for current chat"
  [{{:keys [current-chat-id] :as db} :db
    get-stored-messages :get-stored-messages
    get-stored-user-statuses :get-stored-user-statuses
    get-referenced-messages :get-referenced-messages :as cofx}]
  (when-not (get-in db [:chats current-chat-id :all-loaded?])
    (let [loaded-count        (count (get-in db [:chats current-chat-id :messages]))
          new-messages        (get-stored-messages current-chat-id loaded-count)
          indexed-messages    (index-messages new-messages)
          referenced-messages (index-messages
                               (get-referenced-messages (get-referenced-ids indexed-messages)))
          new-message-ids     (keys indexed-messages)
          new-statuses        (get-stored-user-statuses current-chat-id new-message-ids)]
      (fx/merge cofx
                {:db (-> db
                         (update-in [:chats current-chat-id :messages] merge indexed-messages)
                         (update-in [:chats current-chat-id :message-statuses] merge new-statuses)
                         (update-in [:chats current-chat-id :not-loaded-message-ids]
                                    #(apply disj % new-message-ids))
                         (update-in [:chats current-chat-id :referenced-messages]
                                    #(into (apply dissoc % new-message-ids) referenced-messages))
                         (assoc-in [:chats current-chat-id :all-loaded?]
                                   (> constants/default-number-of-messages (count new-messages))))}
                (group-chat-messages current-chat-id new-messages)
                (chat-model/mark-messages-seen current-chat-id)))))
