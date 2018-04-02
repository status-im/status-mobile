(ns status-im.data-store.transport
  (:require [cljs.tools.reader.edn :as edn]
            [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.transport :as data-store]
            [status-im.data-store.realm.core :as core]))


(defn deserialize-chat [serialized-chat]
  (-> serialized-chat
      (dissoc :chat-id)
      (update :ack edn/read-string)
      (update :seen edn/read-string)
      (update :pending-ack edn/read-string)
      (update :pending-send edn/read-string)))

(re-frame/reg-cofx
  :data-store/transport
  (fn [cofx _]
    (assoc cofx
           :data-store/transport
           (reduce (fn [acc {:keys [chat-id] :as chat}]
                     (assoc acc chat-id (deserialize-chat chat)))
                   {}
                   (data-store/get-all)))))

(defn save [chat-id chat]
  (let [serialized-chat (-> chat
                            (assoc :chat-id chat-id)
                            (update :ack pr-str)
                            (update :seen pr-str)
                            (update :pending-ack pr-str)
                            (update :pending-send pr-str))]
    (data-store/save serialized-chat)))

(re-frame/reg-fx
  :data-store.transport/save
  (fn [{:keys [chat-id chat]}]
    (async/go (async/>! core/realm-queue #(save chat-id chat)))))

(re-frame/reg-fx
  :data-store.transport/delete
  (fn [chat-id]
    (async/go (async/>! core/realm-queue #(data-store/delete chat-id)))))
