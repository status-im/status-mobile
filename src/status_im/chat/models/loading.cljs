(ns status-im.chat.models.loading
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.chat.commands.core :as commands]
            [status-im.transport.filters.core :as filters]
            [status-im.chat.models :as chat-model]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.mailserver.core :as mailserver]
            [status-im.utils.config :as config]
            [status-im.utils.datetime :as time]
            [status-im.utils.fx :as fx]
            [status-im.utils.priority-map :refer [empty-message-map]]
            [taoensso.timbre :as log]))

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
                                   (map (fn [{:keys [message-id timestamp whisper-timestamp]}]
                                          {:message-id        message-id
                                           :timestamp-str     (time/timestamp->time timestamp)
                                           :timestamp         timestamp
                                           :whisper-timestamp whisper-timestamp}))
                                   (into (or message-references '()))
                                   (sort-references (get-in db [:chats chat-id :messages]))))))
               db
               (group-by (comp time/day-relative :timestamp) messages))})

(defn- get-referenced-ids
  "Takes map of `message-id->messages` and returns set of maps of
  `{:response-to-v2 message-id}`,
   excluding any `message-id` which is already in the original map"
  [message-id->messages]
  (into #{}
        (comp (keep (fn [{:keys [content]}]
                      (let [response-to-id
                            (select-keys content [:response-to-v2])]
                        (when (some (complement nil?) (vals response-to-id))
                          response-to-id))))
              (remove #(some message-id->messages (vals %))))
        (vals message-id->messages)))

(fx/defn update-chats-in-app-db
  {:events [:chats-list/load-success]}
  [{:keys [db] :as cofx} new-chats]
  (let [old-chats (:chats db)
        chats (reduce (fn [acc {:keys [chat-id] :as chat}]
                        (assoc acc chat-id
                               (assoc chat
                                      :messages-initialized? false
                                      :messages empty-message-map)))
                      {}
                      new-chats)
        chats (merge old-chats chats)]
    (fx/merge cofx
              {:db (assoc db :chats chats
                          :chats/loading? false)}
              (filters/load-filters)
              (commands/load-commands commands/register))))

(defn load-chats-from-rpc
  [cofx from to]
  (data-store.chats/fetch-chats-rpc cofx {:from 0
                                          :to 10
                                          :on-success
                                          #(re-frame/dispatch
                                            [:chats-list/load-success %])}))
(fx/defn initialize-chats
  "Initialize persisted chats on startup"
  [cofx
   {:keys [from to] :or {from 0 to nil}}]
  (load-chats-from-rpc cofx from -1))

(fx/defn messages-loaded
  "Loads more messages for current chat"
  {:events [::messages-loaded]
   :interceptors [(re-frame/inject-cofx :data-store/all-gaps)]}
  [{{:keys [current-chat-id] :as db} :db :as cofx}
   chat-id
   {:keys [cursor messages]}]
  (when-not (or (nil? current-chat-id)
                (not= chat-id current-chat-id))
    (let [already-loaded-messages    (get-in db [:chats current-chat-id :messages])
          ;; We remove those messages that are already loaded, as we might get some duplicates
          new-messages               (remove (comp already-loaded-messages :message-id)
                                             messages)
          unviewed-message-ids       (reduce
                                      (fn [acc {:keys [seen message-id] :as message}]
                                        (if (not seen)
                                          (conj acc message-id)
                                          acc))
                                      #{}
                                      new-messages)

          indexed-messages           (index-messages new-messages)
          new-message-ids            (keys indexed-messages)]
      (fx/merge cofx
                {:db (-> db
                         (update-in [:chats current-chat-id :loaded-unviewed-messages-ids] clojure.set/union  unviewed-message-ids)
                         (assoc-in [:chats current-chat-id :messages-initialized?] true)
                         (update-in [:chats current-chat-id :messages] merge indexed-messages)
                         (assoc-in [:chats current-chat-id :cursor] cursor)
                         (assoc-in [:chats current-chat-id :all-loaded?]
                                   (empty? cursor)))}
                (mailserver/load-gaps current-chat-id)
                (group-chat-messages current-chat-id new-messages)
                (chat-model/mark-messages-seen current-chat-id)))))

(defn load-more-messages
  [{:keys [db]}]
  (when-let [current-chat-id (:current-chat-id db)]
    (when-not (get-in db [:chats current-chat-id :all-loaded?])
      (let [cursor (get-in db [:chats current-chat-id :cursor])]
        (data-store.messages/messages-by-chat-id-rpc current-chat-id
                                                     cursor
                                                     constants/default-number-of-messages
                                                     #(re-frame/dispatch [::messages-loaded current-chat-id %]))))))
