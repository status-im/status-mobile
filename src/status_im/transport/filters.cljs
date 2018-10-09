(ns ^{:doc "API for whisper filters"}
 status-im.transport.filters
  (:require [re-frame.core :as re-frame]
            [status-im.transport.inbox :as inbox]
            [status-im.transport.utils :as utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [taoensso.timbre :as log]))

(defn remove-filter! [filter]
  (.stopWatching filter
                 (fn [error _]
                   (if error
                     (log/warn :remove-filter-error filter error)
                     (log/debug :removed-filter filter))))
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

(handlers/register-handler-fx
 :shh.callback/filter-added
 (fn [{:keys [db] :as cofx} [_ topic chat-id filter]]
   (fx/merge cofx
             {:db (assoc-in db [:transport/filters chat-id] filter)}
             (inbox/upsert-inbox-topic {:topic topic
                                        :chat-id chat-id}))))

(re-frame/reg-fx
 :shh/remove-filter
 (fn [filter]
   (when filter (remove-filter! filter))))

(re-frame/reg-fx
 :shh/remove-filters
 (fn [filters]
   (doseq [filter filters]
     (when filter (remove-filter! filter)))))
