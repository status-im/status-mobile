(ns ^{:doc "API for whisper filters"}
 status-im.transport.filters
  (:require [re-frame.core :as re-frame]
            [status-im.transport.inbox :as inbox]
            [status-im.transport.utils :as utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [taoensso.timbre :as log]))

(defn remove-filter! [{:keys [chat-id filter]}]
  (.stopWatching filter
                 (fn [error _]
                   (if error
                     (log/warn :remove-filter-error filter error)
                     (re-frame/dispatch [:shh.callback/filter-removed chat-id]))))
  (log/debug :stop-watching filter))

(defn add-filter!
  [web3 {:keys [topics to] :as options} callback chat-id]
  (let [options  (assoc options :allowP2P true)]
    (log/debug :add-filter options)
    (when-let [filter (.newMessageFilter (utils/shh web3)
                                         (clj->js options)
                                         callback
                                         #(log/warn :add-filter-error (.stringify js/JSON (clj->js options)) %))]
      (re-frame/dispatch [:shh.callback/filter-added (first topics) chat-id filter]))))

(re-frame/reg-fx
 :shh/add-filter
 (fn [{:keys [web3 sym-key-id topic chat-id]}]
   (let [params   {:topics [topic]
                   :symKeyID sym-key-id}
         callback (fn [js-error js-message]
                    (re-frame/dispatch [:transport/messages-received js-error js-message chat-id]))]
     (add-filter! web3 params callback chat-id))))

(re-frame/reg-fx
 :shh/add-discovery-filter
 (fn [{:keys [web3 private-key-id topic]}]
   (let [params   {:topics [topic]
                   :privateKeyID private-key-id}
         callback (fn [js-error js-message]
                    (re-frame/dispatch [:transport/messages-received js-error js-message]))]
     (add-filter! web3 params callback :discovery-topic))))

(defn all-filters-added?
  [{:keys [db]}]
  (let [filters (set (keys (get db :transport/filters)))
        chats (into #{:discovery-topic}
                    (keys (filter #(:topic (val %)) (get db :transport/chats))))]
    (= chats filters)))

(handlers/register-handler-fx
 :shh.callback/filter-added
 (fn [{:keys [db] :as cofx} [_ topic chat-id filter]]
   (log/info :debug-filter :topic topic :chat-id chat-id)
   (fx/merge cofx
             {:db (assoc-in db [:transport/filters chat-id] filter)}
             (inbox/reset-request-to)
             (inbox/upsert-inbox-topic {:topic topic
                                        :chat-id chat-id})
             (inbox/process-next-messages-request))))

(handlers/register-handler-fx
 :shh.callback/filter-removed
 (fn [{:keys [db]} [_ chat-id]]
   {:db (update db :transport/filters dissoc chat-id)}))

(re-frame/reg-fx
 :shh/remove-filter
 (fn [{:keys [filter] :as params}]
   (when filter (remove-filter! params))))

(re-frame/reg-fx
 :shh/remove-filters
 (fn [filters]
   (doseq [[chat-id filter] filters]
     (when filter (remove-filter! {:chat-id chat-id
                                   :filter filter})))))
