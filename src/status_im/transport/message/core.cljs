(ns ^{:doc "Definition of the StatusMessage protocol"}
 status-im.transport.message.core
  (:require [goog.object :as o]
            [re-frame.core :as re-frame]
            [status-im.chat.models.message :as models.message]
            [status-im.utils.handlers :as handlers]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.core :as ethereum]
            [status-im.native-module.core :as status]
            [status-im.transport.message.contact :as contact]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.utils :as transport.utils]
            [status-im.tribute-to-talk.whitelist :as whitelist]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.ethereum.json-rpc :as json-rpc]))

(def message-type-message 1)

(defn build-message [parsed-message-js]
  (let [content (.-content parsed-message-js)
        built-message
        (protocol/Message.
         {:text (.-text content)
          :response-to (.-response-to content)
          :chat-id (.-chat_id content)}
         (.-content_type parsed-message-js)
         (keyword (.-message_type parsed-message-js))
         (.-clock parsed-message-js)
         (.-timestamp parsed-message-js))]
    built-message))

(defn handle-message
  "Check if parsedMessage is present and of a supported type, if so
  build a record using the right type. Otherwise defaults to transit
  deserializing"
  [message-js]
  (if (and (.-parsedMessage message-js)
           (= message-type-message) (.-messageType message-js))
    (build-message (.-parsedMessage message-js))
    (transit/deserialize (.-payload message-js))))

(fx/defn receive-message
  "Receive message handles a new status-message.
  dedup-id is passed by status-go and is used to deduplicate messages at that layer.
  Once a message has been successfuly processed, that id needs to be sent back
  in order to stop receiving that message"
  [{:keys [db]} now-in-s filter-chat-id message-js]
  (let [blocked-contacts (get db :contacts/blocked #{})
        timestamp (.-timestamp (.-message message-js))
        metadata-js (.-metadata message-js)
        metadata {:author {:publicKey (.-publicKey (.-author metadata-js))
                           :alias (.-alias (.-author metadata-js))
                           :identicon (.-identicon (.-author metadata-js))}
                  :dedupId (.-dedupId metadata-js)
                  :encryptionId (.-encryptionId metadata-js)
                  :messageId (.-messageId metadata-js)}
        status-message (handle-message message-js)
        sig (-> metadata :author :publicKey)]
    (when (and sig
               status-message
               (not (blocked-contacts sig)))
      (try
        (when-let [valid-message (protocol/validate status-message)]
          (protocol/receive
           (assoc valid-message
                  :metadata metadata)
           (or
            filter-chat-id
            (get-in valid-message [:content :chat-id])
            sig)
           sig
           timestamp
           {:db db
            :metadata metadata}))
        (catch :default e nil))))) ; ignore unknown message types

(defn- js-obj->seq [obj]
  ;; Sometimes the filter will return a single object instead of a collection
  (if (array? obj)
    (for [i (range (.-length obj))]
      (aget obj i))
    [obj]))

(handlers/register-handler-fx
 ::process
 (fn [cofx [_ messages now-in-s]]
   (let [[chat-id message] (first messages)
         remaining-messages (rest messages)]
     (if (seq remaining-messages)
       (assoc
        (receive-message cofx now-in-s chat-id message)
        ;; We dispatch later to let the UI thread handle events, without this
        ;; it will keep processing events ignoring user input.
        :dispatch-later [{:ms 20 :dispatch [::process remaining-messages now-in-s]}])
       (receive-message cofx now-in-s chat-id message)))))

(fx/defn receive-messages
  "Initialize the ::process event, which will process messages one by one
  dispatching later to itself"
  [{:keys [now] :as cofx} event-js]
  (let [now-in-s (quot now 1000)
        events (reduce
                (fn [acc message-specs]
                  (let [chat (.-chat message-specs)
                        messages (.-messages message-specs)
                        error (.-error message-specs)
                        chat-id (if (or (.-discovery chat)
                                        (.-negotiated chat))
                                  nil
                                  (.-chatId chat))]
                    (if (seq messages)
                      (reduce (fn [acc m]
                                (conj acc [chat-id m]))
                              acc
                              messages)
                      acc)))
                []
                (.-messages event-js))]
    {:dispatch [::process events now-in-s]}))

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

(fx/defn resend-contact-request [cofx own-info chat-id {:keys [sym-key topic]}]
  (protocol/send (contact/map->ContactRequest own-info)
                 chat-id cofx))

(re-frame/reg-fx
 :transport/confirm-messages-processed
 (fn [confirmations]
   (when (seq confirmations)
     (json-rpc/call {:method "shhext_confirmMessagesProcessedByID"
                     :params [confirmations]
                     :on-success #(log/debug "successfully confirmed messages")
                     :on-failure #(log/error "failed to confirm messages" %)}))))
