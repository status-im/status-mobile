(ns status-im.chat.models.gaps
  (:require [utils.re-frame :as rf]
            [taoensso.timbre :as log]
            [status-im2.contexts.chat.messages.list.events :as message-list]
            [status-im.mailserver.core :as mailserver]))

(rf/defn gaps-filled
  {:events [:gaps/filled]}
  [{:keys [db] :as cofx} chat-id message-ids]
  (rf/merge
   cofx
   {:db (-> db
            (update-in [:messages chat-id] (fn [messages] (apply dissoc messages message-ids)))
            (dissoc :mailserver/fetching-gaps-in-progress))}
   (message-list/rebuild-message-list chat-id)))

(rf/defn gaps-failed
  {:events [:gaps/failed]}
  [{:keys [db]} chat-id gap-ids error]
  (log/error "failed to fetch gaps" chat-id gap-ids error)
  {:db (dissoc db :mailserver/fetching-gaps-in-progress)})

(rf/defn sync-chat-from-sync-from-failed
  {:events [::sync-chat-from-sync-from-failed]}
  [{:keys [db]} chat-id error]
  (log/error "failed to sync chat" chat-id error)
  {:db (dissoc db :mailserver/fetching-gaps-in-progress)})

(rf/defn sync-chat-from-sync-from-success
  {:events [::sync-chat-from-sync-from-success]}
  [{:keys [db] :as cofx} chat-id synced-from]
  (log/debug "synced success" chat-id synced-from)
  {:db
   (-> db
       (assoc-in [:chats chat-id :synced-from] synced-from)
       (dissoc :mailserver/fetching-gaps-in-progress))})

(rf/defn fill-gaps
  [_ chat-id gap-ids]
  {:json-rpc/call [{:method     "wakuext_fillGaps"
                    :params     [chat-id gap-ids]
                    :on-success #(rf/dispatch [:gaps/filled chat-id gap-ids %])
                    :on-error   #(rf/dispatch [:gaps/failed chat-id gap-ids %])}]})

(rf/defn sync-chat-from-sync-from
  [_ chat-id]
  (log/debug "syncing chat from sync from")
  {:json-rpc/call [{:method     "wakuext_syncChatFromSyncedFrom"
                    :params     [chat-id]
                    :on-success #(rf/dispatch [::sync-chat-from-sync-from-success chat-id %])
                    :on-error   #(rf/dispatch [::sync-chat-from-sync-from-failed chat-id %])}]})

(rf/defn chat-ui-fill-gaps
  {:events [:chat.ui/fill-gaps]}
  [{:keys [db] :as cofx} chat-id gap-ids]
  (let [use-status-nodes? (mailserver/fetch-use-mailservers? {:db db})]
    (log/info "filling gaps if use-status-nodes = true" chat-id gap-ids)
    (when use-status-nodes?
      (rf/merge cofx
                {:db (assoc db :mailserver/fetching-gaps-in-progress gap-ids)}
                (if (= gap-ids #{:first-gap})
                  (sync-chat-from-sync-from chat-id)
                  (fill-gaps chat-id gap-ids))))))
