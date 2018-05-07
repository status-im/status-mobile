(ns ^{:doc "API for whisper filters"}
 status-im.transport.filters
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.transport.utils :as utils]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]))

(defn- receive-message [chat-id js-error js-message]
  (re-frame/dispatch [:protocol/receive-whisper-message js-error js-message chat-id]))

(defn remove-filter! [filter]
  (.stopWatching filter
                 (fn [error _]
                   (when error
                     (log/warn :remove-filter-error filter error))))
  (log/debug :stop-watching filter))

(defn add-shh-filter!
  [web3 options callback]
  (.newMessageFilter (utils/shh web3) (clj->js options)
                     callback
                     #(log/warn :add-filter-error (.stringify js/JSON (clj->js options)) %)))

(defn add-filter!
  [web3 {:keys [chat-id topics to event] :as options} callback]
  (let [options  (if config/offline-inbox-enabled?
                   (assoc options :allowP2P true)
                   options)]
    (log/debug :add-filter options)
    (if-let [filter (add-shh-filter! web3 options callback)]
      (re-frame/dispatch [event chat-id filter])
      (log/error "Could not create filter for" options))))

(re-frame/reg-fx
 :shh/add-filter
 (fn [{:keys [web3 sym-key-id topic chat-id one-to-one]}]
   (add-filter!
    web3
    {:topics  [topic]
     :event ::filter-added
     :chat-id chat-id
     :symKeyID sym-key-id}
    (partial receive-message chat-id))
   ;; We add a noop filter to avoid identification
   (when one-to-one
     (add-filter!
      web3
      {:topics [(utils/get-topic chat-id)]
       :event  ::filter-added
       :chat-id chat-id
       :minPow 1
       :symKeyId sym-key-id}
      (constantly nil)))))

(handlers/register-handler-db
 ::filter-added
 [re-frame/trim-v]
 (fn [db [chat-id filter]]
   (update-in db [:transport/chats chat-id :filters] conj filter)))

(re-frame/reg-fx
 :shh/add-discovery-filter
 (fn [{:keys [web3 private-key-id topic]}]
   (add-filter! web3
                {:topics [topic]
                 :event ::discovery-filter-added
                 :privateKeyID private-key-id}
                (partial receive-message nil))))

(handlers/register-handler-db
 ::discovery-filter-added
 [re-frame/trim-v]
 (fn [db [_ filter]]
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
