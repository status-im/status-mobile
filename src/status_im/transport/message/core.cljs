(ns ^{:doc "Definition of the StatusMessage protocol"}
 status-im.transport.message.core
  (:require [goog.object :as o]
            [re-frame.core :as re-frame]
            [status-im.chat.models.message :as models.message]
            [status-im.contact.device-info :as device-info]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.core :as ethereum]
            [status-im.transport.message.contact :as contact]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.ethereum.json-rpc :as json-rpc]))

(defn add-raw-payload
  "Add raw payload for id calculation"
  [{:keys [message] :as m}]
  (assoc m :raw-payload (clj->js message)))

(fx/defn receive-message
  "Receive message handles a new status-message.
  dedup-id is passed by status-go and is used to deduplicate messages at that layer.
  Once a message has been successfuly processed, that id needs to be sent back
  in order to stop receiving that message"
  [cofx now-in-s filter-chat-id message-js]
  (let [blocked-contacts (get-in cofx [:db :contacts/blocked] #{})
        payload (.-payload (.-message message-js))
        timestamp (.-timestamp (.-message message-js))
        metadata-js (.-metadata message-js)
        metadata {:author {:publicKey (.-publicKey (.-author metadata-js))
                           :alias (.-alias (.-author metadata-js))
                           :identicon (.-identicon (.-author metadata-js))}
                  :dedupId (.-dedupId metadata-js)
                  :encryptionId (.-encryptionId metadata-js)
                  :messageId (.-messageId metadata-js)}
        raw-payload  {:raw-payload message-js}
        status-message (-> payload
                           ethereum/hex-to-utf8
                           transit/deserialize)
        sig (-> metadata :author :publicKey)]
    (println "PAYLOAD")
    (println "METADATA" metadata)
    (println "RAW PAYLOAD" raw-payload)
    (when (and sig
               status-message
               (not (blocked-contacts sig)))
      (try
        (when-let [valid-message (protocol/validate status-message)]
          (fx/merge (assoc cofx :js-obj raw-payload :metadata metadata)
                    #(protocol/receive (assoc valid-message
                                              :metadata metadata)
                                       (or
                                        filter-chat-id
                                        (get-in valid-message [:content :chat-id])
                                        sig)
                                       sig
                                       timestamp
                                       %)))
        (catch :default e nil))))) ; ignore unknown message types

(defn- js-obj->seq [obj]
  ;; Sometimes the filter will return a single object instead of a collection
  (if (array? obj)
    (for [i (range (.-length obj))]
      (aget obj i))
    [obj]))

(fx/defn receive-whisper-messages
  [{:keys [now] :as cofx} error messages chat-id]
  (if (and (not error)
           messages)
    (let [now-in-s (quot now 1000)
          receive-message-fxs (map (fn [message]
                                     (receive-message now-in-s chat-id message))
                                   messages)]
      (apply fx/merge cofx receive-message-fxs))
    (log/error "Something went wrong" error messages)))

(fx/defn receive-messages [cofx event-js]
  (let [fxs (keep
             (fn [a]
               (let [chat (.-chat a)
                     messages (.-messages a)
                     error (.-error a)]
                 (when (seq messages)
                   (receive-whisper-messages
                    error
                    messages
                  ;; For discovery and negotiated filters we don't
                  ;; set a chatID, and we use the signature of the message
                  ;; to indicate which chat it is for
                    (if (or (.-discovery chat)
                            (.-negotiated chat))
                      nil
                      (.-chatId chat))))))
             (.-messages event-js))]
    (apply fx/merge cofx fxs)))

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
        (let [{:keys [fcm-token]} (get-in db [:contacts/contacts chat-id])
              ;; We pick the last max-installations devices
              fcm-tokens
              (as-> (get-in db [:contacts/contacts chat-id :device-info]) $
                (vals $)
                (sort-by :timestamp $)
                (reverse $)
                (map :fcm-token $)
                (into #{} $)
                (conj $ fcm-token)
                (filter identity $)
                (take (inc config/max-installations) $))]
          (fx/merge cofx
                    (check-confirmations status chat-id message-id)
                    (models.message/send-push-notification chat-id message-id fcm-tokens status)))))))

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
  (let [{:keys [name photo-path address]} (:multiaccount db)
        fcm-token (get-in db [:notifications :fcm-token])]
    {:name          name
     :profile-image photo-path
     :address       address
     :device-info   (device-info/all {:db db})
     :fcm-token     fcm-token}))

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
