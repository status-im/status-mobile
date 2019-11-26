(ns ^{:doc "Definition of the StatusMessage protocol"}
 status-im.transport.message.core
  (:require [goog.object :as o]
            [re-frame.core :as re-frame]
            [status-im.chat.models.message :as models.message]
            [status-im.chat.models :as models.chat]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.constants :as constants]
            [status-im.utils.handlers :as handlers]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.core :as ethereum]
            [status-im.native-module.core :as status]
            [status-im.transport.message.contact :as contact]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.utils :as transport.utils]
            [status-im.tribute-to-talk.whitelist :as whitelist]
            [status-im.ens.core :as ens]
            [cljs-bean.core :as clj-bean]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.ethereum.json-rpc :as json-rpc]))

(fx/defn handle-raw-message
  "Receive message handles a new status-message.
  dedup-id is passed by status-go and is used to deduplicate messages at that layer.
  Once a message has been successfuly processed, that id needs to be sent back
  in order to stop receiving that message"
  [{:keys [db] :as cofx} raw-message-js]
  (let [timestamp (.-timestamp raw-message-js)
        sig (.-from raw-message-js)
        payload (.-payload raw-message-js)]
    (let [status-message (transit/deserialize payload)]
      (when (and sig
                 status-message)
        (try
          (when-let [valid-message (protocol/validate status-message)]
            (protocol/receive
             valid-message
             sig
             sig
             timestamp
             cofx))
          (catch :default e nil)))))) ; ignore unknown message types

(defn- js-obj->seq [obj]
  ;; Sometimes the filter will return a single object instead of a collection
  (if (array? obj)
    (for [i (range (.-length obj))]
      (aget obj i))
    [obj]))

(fx/defn handle-chat [cofx chat]
  ;; :unviewed-messages-count is managed by status-react, so we don't copy
  ;; over it
  (models.chat/ensure-chat cofx (dissoc chat :unviewed-messages-count)))

(fx/defn handle-message-2 [cofx message]
  (fx/merge cofx
            (models.message/receive-one message)
            (ens/verify-names-from-message message (:from message))))

(fx/defn process-response [cofx response-js]
  (let [chats (.-chats response-js)
        raw-messages (.-rawMessages response-js)
        messages (.-messages response-js)]
    (cond
      (seq chats)
      (let [chat (.pop chats)]
        (fx/merge cofx
                  {:dispatch-later [{:ms 20 :dispatch [::process response-js]}]}
                  (handle-chat (-> chat (clj-bean/->clj) (data-store.chats/<-rpc)))))
      (seq raw-messages)
      (let [first-filter (aget raw-messages 0)
            messages     (.-messages first-filter)
            first-message (.pop messages)]
       ;; Pop the empty array
        (when (= (.-length messages) 0)
          (.pop raw-messages))
        (when first-message
          (fx/merge cofx
                    {:dispatch-later [{:ms 20 :dispatch [::process response-js]}]}
                    (handle-raw-message first-message))))

      (seq messages)
      (let [message (.pop messages)]
        (fx/merge cofx
                  {:dispatch-later [{:ms 20 :dispatch [::process response-js]}]}
                  (handle-message-2 (-> message (clj-bean/->clj) (data-store.messages/<-rpc))))))))

(handlers/register-handler-fx
 ::process
 (fn [cofx [_ response-js]]
   (process-response cofx response-js)))

(fx/defn remove-hash
  [{:keys [db] :as cofx} envelope-hash]
  {:db (update db :transport/message-envelopes dissoc envelope-hash)})

(fx/defn check-confirmations
  [{:keys [db] :as cofx} status chat-id message-id]
  (when-let [{:keys [pending-confirmations not-sent]}
             (get-in db [:transport/message-ids->confirmations message-id])]
    (if (zero? (dec pending-confirmations))
      (fx/merge cofx
                {:db (update db
                             :transport/message-ids->confirmations
                             dissoc message-id)}
                (models.message/update-message-status chat-id
                                                      message-id
                                                      (if not-sent
                                                        :not-sent
                                                        status))
                (remove-hash message-id))
      (let [confirmations {:pending-confirmations (dec pending-confirmations)
                           :not-sent  (or not-sent
                                          (= :not-sent status))}]
        {:db (assoc-in db
                       [:transport/message-ids->confirmations message-id]
                       confirmations)}))))

(fx/defn update-envelope-status
  [{:keys [db] :as cofx} envelope-hash status]
  (let [{:keys [chat-id message-type message-id]}
        (get-in db [:transport/message-envelopes envelope-hash])]
    (case message-type
      :contact-message
      (when (= :sent status)
        (remove-hash cofx envelope-hash))

      (when-let [{:keys [from]} (get-in db [:chats chat-id :messages message-id])]
        (check-confirmations cofx status chat-id message-id)))))

(fx/defn update-envelopes-status
  [{:keys [db] :as cofx} envelope-hashes status]
  (apply fx/merge cofx (map #(update-envelope-status % status) envelope-hashes)))

(fx/defn set-contact-message-envelope-hash
  [{:keys [db] :as cofx} chat-id envelope-hash]
  {:db (assoc-in db [:transport/message-envelopes envelope-hash]
                 {:chat-id      chat-id
                  :message-type :contact-message})})

(fx/defn set-message-envelope-hash
  "message-type is used for tracking"
  [{:keys [db] :as cofx} chat-id message-id message-type messages-count]
  {:db (-> db
           (assoc-in [:transport/message-envelopes message-id]
                     {:chat-id      chat-id
                      :message-id   message-id
                      :message-type message-type})
           (update-in [:transport/message-ids->confirmations message-id]
                      #(or % {:pending-confirmations messages-count})))})

(defn- own-info [db]
  (let [{:keys [name photo-path address]} (:multiaccount db)]
    {:name          name
     :profile-image photo-path
     :address       address}))
