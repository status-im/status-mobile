(ns ^{:doc "Definition of the StatusMessage protocol"}
 status-im.transport.message.core
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models.message :as models.message]
            [status-im.utils.config :as config]
            [status-im.data-store.transport :as transport-store]
            [status-im.transport.message.contact :as contact]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.utils :as transport.utils]
            [status-im.contact.device-info :as device-info]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(fx/defn receive-message
  [cofx now-in-s filter-chat-id js-message]
  (let [blocked-contacts (get-in cofx [:db :contacts/blocked] #{})
        {:keys [payload sig timestamp ttl]} (js->clj js-message :keywordize-keys true)
        status-message (-> payload
                           transport.utils/to-utf8
                           transit/deserialize)]
    (when (and sig
               status-message
               (not (blocked-contacts sig)))
      (try
        (when-let [valid-message (protocol/validate status-message)]
          (fx/merge (assoc cofx :js-obj js-message)
                    #(protocol/receive valid-message
                                       (or
                                        filter-chat-id
                                        (get-in valid-message [:content :chat-id])
                                        sig)
                                       sig
                                       timestamp
                                       %)))
        (catch :default e nil))))) ; ignore unknown message types

(defn- js-array->seq [array]
  (for [i (range (.-length array))]
    (aget array i)))

(fx/defn receive-whisper-messages
  [{:keys [now] :as cofx} js-error js-messages chat-id]
  (if (and (not js-error)
           js-messages)
    (let [now-in-s (quot now 1000)
          receive-message-fxs (map (fn [message]
                                     (receive-message now-in-s chat-id message))
                                   (js-array->seq js-messages))]
      (apply fx/merge cofx receive-message-fxs))
    (log/error "Something went wrong" js-error js-messages)))

(fx/defn remove-hash
  [{:keys [db] :as cofx} envelope-hash]
  {:db (update db :transport/message-envelopes dissoc envelope-hash)})

(fx/defn update-resend-contact-message
  [{:keys [db] :as cofx} chat-id]
  (let [chat         (get-in db [:transport/chats chat-id])
        updated-chat (assoc chat :resend? nil)]
    {:db            (assoc-in db [:transport/chats chat-id :resend?] nil)
     :data-store/tx [(transport-store/save-transport-tx {:chat-id chat-id
                                                         :chat    updated-chat})]}))
(fx/defn check-confirmations [{:keys [db] :as cofx} status chat-id message-id]
  (when-let [{:keys [pending-confirmations not-sent]}
             (get-in db [:transport/message-ids->confirmations message-id])]
    (if (zero? (dec pending-confirmations))
      (fx/merge cofx
                {:db (update db :transport/message-ids->confirmations dissoc message-id)}
                (models.message/update-message-status chat-id message-id (if not-sent
                                                                           :not-sent
                                                                           status)))
      (let [confirmations {:pending-confirmations (dec pending-confirmations)
                           :not-sent  (or not-sent
                                          (= :not-sent status))}]
        {:db (assoc-in db [:transport/message-ids->confirmations message-id] confirmations)}))))

(fx/defn update-envelope-status
  [{:keys [db] :as cofx} envelope-hash status]
  (let [{:keys [chat-id message-type message-id]}
        (get-in db [:transport/message-envelopes envelope-hash])]
    (case message-type
      :contact-message
      (when (= :sent status)
        (fx/merge cofx
                  (remove-hash envelope-hash)
                  (update-resend-contact-message chat-id)))

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
                    (remove-hash envelope-hash)
                    (check-confirmations status chat-id message-id)
                    (models.message/send-push-notification chat-id message-id fcm-tokens status)))))))

(fx/defn set-contact-message-envelope-hash
  [{:keys [db] :as cofx} chat-id envelope-hash]
  {:db (assoc-in db [:transport/message-envelopes envelope-hash]
                 {:chat-id      chat-id
                  :message-type :contact-message})})

(fx/defn set-message-envelope-hash
  "message-type is used for tracking"
  [{:keys [db] :as cofx} chat-id message-id message-type envelope-hash-js messages-count]
  (let [envelope-hash (js->clj envelope-hash-js)
        hash (if (vector? envelope-hash)
               (last envelope-hash)
               envelope-hash)]
    {:db (-> db
             (assoc-in [:transport/message-envelopes hash]
                       {:chat-id      chat-id
                        :message-id   message-id
                        :message-type message-type})
             (update-in [:transport/message-ids->confirmations message-id]
                        #(or % {:pending-confirmations messages-count})))}))

(defn- own-info [db]
  (let [{:keys [name photo-path address]} (:account/account db)
        fcm-token (get-in db [:notifications :fcm-token])]
    {:name          name
     :profile-image photo-path
     :address       address
     :device-info   (device-info/all {:db db})
     :fcm-token     fcm-token}))

(fx/defn resend-contact-request [cofx own-info chat-id {:keys [sym-key topic]}]
  (protocol/send (contact/map->ContactRequest own-info)
                 chat-id cofx))

(fx/defn resend-contact-message
  [cofx own-info chat-id]
  (let [{:keys [resend?] :as chat} (get-in cofx [:db :transport/chats chat-id])]
    (case resend?
      "contact-request"
      (resend-contact-request cofx own-info chat-id chat)
      "contact-request-confirmation"
      (protocol/send (contact/map->ContactRequestConfirmed own-info)
                     chat-id
                     cofx)
      "contact-update"
      (protocol/send-with-pubkey cofx
                                 {:chat-id chat-id
                                  :payload (contact/map->ContactUpdate own-info)})
      nil)))

(fx/defn resend-contact-messages
  [{:keys [db] :as cofx} previous-summary]
  (when (and (zero? (count previous-summary))
             (= :online (:network-status db))
             (pos? (count (:peers-summary db))))
    (let [own-info (own-info db)
          resend-contact-message-fxs (map (fn [chat-id]
                                            (resend-contact-message own-info chat-id))
                                          (keys (:transport/chats db)))]
      (apply fx/merge cofx resend-contact-message-fxs))))

(re-frame/reg-fx
 :transport/confirm-messages-processed
 (fn [messages]
   (let [{:keys [web3]} (first messages)
         js-messages (->> messages
                          (keep :js-obj)
                          (apply array))]
     (when (pos? (.-length js-messages))
       (.confirmMessagesProcessed (transport.utils/shh web3)
                                  js-messages
                                  (fn [err resp]
                                    (when err
                                      (log/info "Confirming messages processed failed"))))))))
