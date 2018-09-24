(ns ^{:doc "Events for message handling"}
 status-im.transport.handlers
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models.message :as models.message]
            [status-im.data-store.transport :as transport-store]
            [status-im.transport.message.core :as message]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.message.v1.contact :as v1.contact]
            [status-im.transport.message.v1.group-chat :as v1.group-chat]
            [status-im.transport.shh :as shh]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [taoensso.timbre :as log]
            [status-im.transport.message.v1.protocol :as protocol]))

(defn validate-chat-id
  "Check the signing user has permission to write to that chat and return
  a valid chat-id"
  [chat-id signature cofx]
  (cond

   ;; one-to-one
    (or (nil? chat-id)
        (= chat-id signature))
    chat-id

   ;; public chat
    (get-in cofx [:db :chats chat-id :public?])
    chat-id

   ;; group chat
    (get-in cofx [:db :chats chat-id :contacts signature])
    chat-id))

(defn update-last-received-from-inbox
  "Distinguishes messages that are expired from those that are not
   Expired messages are coming from offline inboxing"
  [now-in-s timestamp ttl {:keys [db now] :as cofx}]
  (when (> (- now-in-s timestamp) ttl)
    {:db (assoc db :inbox/last-received now)}))

(defn receive-message [cofx now-in-s chat-id js-message]
  (let [{:keys [payload sig timestamp ttl]} (js->clj js-message :keywordize-keys true)
        status-message (-> payload
                           transport.utils/to-utf8
                           transit/deserialize)]
    (when (and sig status-message)
      (try
        (if-let [valid-chat-id (validate-chat-id (or chat-id
                                                     (:chat-id status-message))
                                                 sig
                                                 cofx)]
          (handlers-macro/merge-fx
           (assoc cofx :js-obj js-message)
           (message/receive status-message valid-chat-id sig timestamp)
           (update-last-received-from-inbox now-in-s timestamp ttl)))
        (catch :default e nil))))) ; ignore unknown message types

(defn- js-array->seq [array]
  (for [i (range (.-length array))]
    (aget array i)))

(defn receive-whisper-messages [{:keys [now] :as cofx} [_ js-error js-messages chat-id]]
  (if (and (not js-error)
           js-messages)
    (let [now-in-s (quot now 1000)]
      (handlers-macro/merge-effects
       cofx
       (fn [message temp-cofx]
         (receive-message temp-cofx now-in-s chat-id message))
       (js-array->seq js-messages)))
    (log/error "Something went wrong" js-error js-messages)))

(handlers/register-handler-fx
 :protocol/receive-whisper-message
 [handlers/logged-in
  (re-frame/inject-cofx :random-id)]
 receive-whisper-messages)

(handlers/register-handler-fx
 :protocol/send-status-message-error
 (fn [{:keys [db] :as cofx} [_ err]]
   (log/error :send-status-message-error err)))

(handlers/register-handler-fx
 :contact/send-new-sym-key
 (fn [{:keys [db random-id] :as cofx} [_ {:keys [chat-id topic message sym-key sym-key-id]}]]
   (let [{:keys [web3 current-public-key]} db
         chat-transport-info               (-> (get-in db [:transport/chats chat-id])
                                               (assoc :sym-key-id sym-key-id
                                                      :sym-key sym-key
                                                      :topic topic))]
     (handlers-macro/merge-fx cofx
                              {:db (assoc-in db [:transport/chats chat-id] chat-transport-info)
                               :shh/add-filter {:web3       web3
                                                :sym-key-id sym-key-id
                                                :topic      topic
                                                :chat-id    chat-id}
                               :data-store/tx  [(transport-store/save-transport-tx {:chat-id chat-id
                                                                                    :chat    chat-transport-info})]}
                              (message/send (v1.contact/NewContactKey. sym-key topic message)
                                            chat-id)))))

(handlers/register-handler-fx
 :contact/add-new-sym-key
 (fn [{:keys [db] :as cofx} [_ {:keys [sym-key-id sym-key chat-id topic timestamp message]}]]
   (let [{:keys [web3 current-public-key]} db
         chat-transport-info               (-> (get-in db [:transport/chats chat-id])
                                               (assoc :sym-key-id sym-key-id
                                                      :sym-key sym-key
                                                      :topic topic))]
     (handlers-macro/merge-fx cofx
                              {:db             (assoc-in db
                                                         [:transport/chats chat-id]
                                                         chat-transport-info)
                               :dispatch       [:inbox/request-chat-history chat-id]
                               :shh/add-filter {:web3       web3
                                                :sym-key-id sym-key-id
                                                :topic      topic
                                                :chat-id    chat-id}
                               :data-store/tx  [(transport-store/save-transport-tx {:chat-id chat-id
                                                                                    :chat    chat-transport-info})]}
                              (message/receive message chat-id chat-id timestamp)))))

#_(handlers/register-handler-fx
   :send-test-message
   (fn [cofx [this timer chat-id n]]
     (if (zero? n)
       (println  "Time: " (str (- (inst-ms (js/Date.)) @timer)))
       (handlers-macro/merge-fx cofx
                                {:dispatch [this timer chat-id (dec n)]}
                                (message/send (protocol/map->Message {:content      (str n)
                                                                      :content-type "text/plain"
                                                                      :message-type :user-message
                                                                      :clock-value  n
                                                                      :timestamp    (str (inst-ms (js/Date.)))})
                                              chat-id)))))

(handlers/register-handler-fx
 :group/unsubscribe-from-chat
 (fn [cofx [_ chat-id]]
   (transport.utils/unsubscribe-from-chat chat-id cofx)))

(re-frame/reg-fx
 ;; TODO(janherich): this should be called after `:data-store/tx` actually
 :confirm-messages-processed
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

(handlers/register-handler-fx
 :transport/set-message-envelope-hash
 ;; message-type is used for tracking
 (fn [{:keys [db]} [_ chat-id message-id message-type envelope-hash-js]]
   ;; TODO (cammellos): For group messages it returns multiple hashes, for now
   ;; we naively assume that if one is sent the batch is ok
   (let [envelope-hash (js->clj envelope-hash-js)
         hash (if (vector? envelope-hash)
                (last envelope-hash)
                envelope-hash)]
     {:db (assoc-in db [:transport/message-envelopes hash]
                    {:chat-id      chat-id
                     :message-id   message-id
                     :message-type message-type})})))

(handlers/register-handler-fx
 :transport/set-contact-message-envelope-hash
 (fn [{:keys [db]} [_ chat-id envelope-hash]]
   {:db (assoc-in db [:transport/message-envelopes envelope-hash]
                  {:chat-id      chat-id
                   :message-type :contact-message})}))

(defn remove-hash [envelope-hash {:keys [db] :as cofx}]
  {:db (update db :transport/message-envelopes dissoc envelope-hash)})

(defn update-resend-contact-message [chat-id {:keys [db] :as cofx}]
  (let [chat         (get-in db [:transport/chats chat-id])
        updated-chat (assoc chat :resend? nil)]
    {:db            (assoc-in db [:transport/chats chat-id :resend?] nil)
     :data-store/tx [(transport-store/save-transport-tx {:chat-id chat-id
                                                         :chat    updated-chat})]}))

(defn update-envelope-status
  [envelope-hash status {:keys [db] :as cofx}]
  (let [{:keys [chat-id message-type message-id]}
        (get-in db [:transport/message-envelopes envelope-hash])]
    (case message-type
      :contact-message
      (when (= :sent status)
        (handlers-macro/merge-fx cofx
                                 (remove-hash envelope-hash)
                                 (update-resend-contact-message chat-id)))

      (when-let [message (get-in db [:chats chat-id :messages message-id])]
        (let [{:keys [fcm-token]} (get-in db [:contacts/contacts chat-id])]
          (handlers-macro/merge-fx cofx
                                   (remove-hash envelope-hash)
                                   (models.message/update-message-status message status)
                                   (models.message/send-push-notification fcm-token status)))))))

(defn- own-info [db]
  (let [{:keys [name photo-path address]} (:account/account db)
        fcm-token (get-in db [:notifications :fcm-token])]
    {:name          name
     :profile-image photo-path
     :address       address
     :fcm-token     fcm-token}))

(defn resend-contact-request [own-info chat-id {:keys [sym-key topic]} cofx]
  (message/send (v1.contact/NewContactKey. sym-key
                                           topic
                                           (v1.contact/map->ContactRequest own-info))
                chat-id cofx))

(defn resend-contact-messages
  ([cofx]
   (resend-contact-messages [] cofx))
  ([previous-summary {:keys [db] :as cofx}]
   (when (and (zero? (count previous-summary))
              (= :online (:network-status db))
              (pos? (count (:peers-summary db))))
     (let [own-info (own-info db)]
       (handlers-macro/merge-effects
        cofx
        (fn [[chat-id {:keys [resend?] :as chat}] temp-cofx]
          (case resend?
            "contact-request"
            (resend-contact-request own-info chat-id chat temp-cofx)
            "contact-request-confirmation"
            (message/send (v1.contact/map->ContactRequestConfirmed own-info)
                          chat-id temp-cofx)
            "contact-update"
            (protocol/send {:chat-id chat-id
                            :payload (v1.contact/map->ContactUpdate own-info)}
                           temp-cofx)
            nil))
        (:transport/chats db))))))
