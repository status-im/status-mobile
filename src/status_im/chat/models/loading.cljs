(ns status-im.chat.models.loading
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.transport.filters.core :as filters]
            [status-im.chat.models :as chat-model]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.mailserver.core :as mailserver]
            [status-im.utils.config :as config]
            [status-im.utils.datetime :as time]
            [status-im.utils.fx :as fx]
            [status-im.utils.priority-map :refer [empty-message-map]]
            [status-im.chat.models.message-list :as message-list]
            [taoensso.timbre :as log]))

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
              (filters/load-filters))))

(fx/defn initialize-chats
  "Initialize persisted chats on startup"
  [cofx]
  (data-store.chats/fetch-chats-rpc cofx {:on-success
                                          #(re-frame/dispatch
                                            [:chats-list/load-success %])}))

(fx/defn messages-loaded
  "Loads more messages for current chat"
  {:events [::messages-loaded]}
  [{{:keys [current-chat-id] :as db} :db :as cofx}
   chat-id
   {:keys [cursor messages]}]
  (when-not (or (nil? current-chat-id)
                (not= chat-id current-chat-id))
    (let [already-loaded-messages    (get-in db [:chats current-chat-id :messages])
          loaded-unviewed-messages-ids (get-in db [:chats current-chat-id :loaded-unviewed-messages-ids] #{})
          ;; We remove those messages that are already loaded, as we might get some duplicates
          {:keys [all-messages
                  new-messages
                  unviewed-message-ids]} (reduce (fn [{:keys [all-messages] :as acc}
                                                      {:keys [seen message-id] :as message}]
                                                   (cond-> acc
                                                     (not seen)
                                                     (update :unviewed-message-ids conj message-id)

                                                     (nil? (get all-messages message-id))
                                                     (update :new-messages conj message)

                                                     :always
                                                     (update :all-messages assoc message-id message)))
                                                 {:all-messages already-loaded-messages
                                                  :unviewed-message-ids loaded-unviewed-messages-ids
                                                  :new-messages []}
                                                 messages)]
      (fx/merge cofx
                {:db (-> db
                         (assoc-in [:chats current-chat-id :loaded-unviewed-messages-ids] unviewed-message-ids)
                         (assoc-in [:chats current-chat-id :messages-initialized?] true)
                         (assoc-in [:chats current-chat-id :messages] all-messages)
                         (update-in [:chats current-chat-id :message-list] message-list/add-many new-messages)
                         (assoc-in [:chats current-chat-id :cursor] cursor)
                         (assoc-in [:chats current-chat-id :all-loaded?]
                                   (empty? cursor)))}
                (chat-model/mark-messages-seen current-chat-id)))))

(fx/defn load-more-messages
  [{:keys [db]}]
  (when-let [current-chat-id (:current-chat-id db)]
    (when-not (get-in db [:chats current-chat-id :all-loaded?])
      (let [cursor (get-in db [:chats current-chat-id :cursor])]
        (data-store.messages/messages-by-chat-id-rpc current-chat-id
                                                     cursor
                                                     constants/default-number-of-messages
                                                     #(re-frame/dispatch [::messages-loaded current-chat-id %]))))))
