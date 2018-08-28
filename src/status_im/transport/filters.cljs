(ns ^{:doc "API for whisper filters"}
 status-im.transport.filters
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.transport.utils :as utils]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]))

(defn remove-filter! [filter]
  (.stopWatching filter
                 (fn [error _]
                   (if error
                     (log/warn :remove-filter-error filter error)
                     (log/debug :removed-filter filter))))
  (log/debug :stop-watching filter))

(defn add-shh-filter!
  [web3 options callback]
  (.newMessageFilter (utils/shh web3) (clj->js options)
                     callback
                     #(log/warn :add-filter-error (.stringify js/JSON (clj->js options)) %)))

(defn add-filter!
  [web3 {:keys [topics to] :as options} callback]
  (let [options  (assoc options :allowP2P true)]
    (log/debug :add-filter options)
    (add-shh-filter! web3 options callback)))

(re-frame/reg-fx
 :shh/add-filter
 (fn [{:keys [web3 sym-key-id topic chat-id]}]
   (when-let [filter (add-filter! web3
                                  {:topics [topic]
                                   :symKeyID sym-key-id}
                                  (fn [js-error js-message]
                                    (re-frame/dispatch [:protocol/receive-whisper-message js-error js-message chat-id])))]
     (re-frame/dispatch [::filter-added chat-id filter]))))

(handlers/register-handler-db
 ::filter-added
 [re-frame/trim-v]
 (fn [db [chat-id filter]]
   (assoc-in db [:transport/chats chat-id :filter] filter)))

(re-frame/reg-fx
 :shh/add-discovery-filter
 (fn [{:keys [web3 private-key-id topic]}]
   (when-let [filter (add-filter! web3
                                  {:topics [topic]
                                   :privateKeyID private-key-id}
                                  (fn [js-error js-message]
                                    (re-frame/dispatch [:protocol/receive-whisper-message js-error js-message])))]
     (re-frame/dispatch [::discovery-filter-added filter]))))

(handlers/register-handler-db
 ::discovery-filter-added
 [re-frame/trim-v]
 (fn [db [filter]]
   (assoc db :transport/discovery-filter filter)))

(re-frame/reg-fx
 :shh/remove-filter
 (fn [filter]
   (when filter (remove-filter! filter))))

(re-frame/reg-fx
 :shh/remove-filters
 (fn [filters]
   (doseq [filter filters]
     (remove-filter! filter))))
