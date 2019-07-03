(ns ^{:doc "Protocol API and protocol utils"}
 status-im.transport.message.protocol
  (:require [cljs.spec.alpha :as spec]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.transport.db :as transport.db]
            [status-im.data-store.transport :as transport-store]
            [status-im.transport.utils :as transport.utils]
            [status-im.tribute-to-talk.whitelist :as whitelist]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn has-paired-installations? [cofx]
  (->>
   (get-in cofx [:db :pairing/installations])
   vals
   (some :enabled?)))

(defn discovery-topic-hash [] (transport.utils/get-topic constants/contact-discovery))

(defprotocol StatusMessage
  "Protocol for the messages that are sent through the transport layer"
  (send [this chat-id cofx] "Method producing all effects necessary for sending the message record")
  (receive [this chat-id signature timestamp cofx] "Method producing all effects necessary for receiving the message record")
  (validate [this] "Method returning the message if it is valid or nil if it is not"))

(def whisper-opts
  {;; time drift that is tolerated by whisper, in seconds
   :whisper-drift-tolerance 10
   ;; ttl of 10 sec
   :ttl                     10
   :powTarget               config/pow-target
   :powTime                 config/pow-time})

(fx/defn init-chat
  "Initialises chat on protocol layer."
  [{:keys [db now]}
   {:keys [chat-id resend?]}]
  (let [transport-chat (transport.db/create-chat {:resend?    resend?})]
    {:db (assoc-in db
                   [:transport/chats chat-id]
                   transport-chat)

     :data-store/tx [(transport-store/save-transport-tx {:chat-id chat-id
                                                         :chat    transport-chat})]}))

(fx/defn remove-chat
  [{:keys [db]} chat-id]
  (when (get-in db [:transport/chats chat-id])
    {:db                 (update db :transport/chats dissoc chat-id)
     :data-store/tx      [(transport-store/delete-transport-tx chat-id)]}))

(defn send-public-message
  "Sends the payload to topic"
  [{:keys [db] :as cofx} chat-id success-event payload]
  (let [{:keys [web3]} db]
    {:shh/send-public-message [{:web3 web3
                                :success-event success-event
                                :src     (multiaccounts.model/current-public-key cofx)
                                :chat    chat-id
                                :payload payload}]}))

(fx/defn send-direct-message
  "Sends the payload using to dst"
  [{:keys [db] :as cofx} dst success-event payload]
  (let [{:keys [web3]} db]
    {:shh/send-direct-message [{:web3           web3
                                :success-event  success-event
                                :src            (multiaccounts.model/current-public-key cofx)
                                :dst            dst
                                :payload        payload}]}))

(fx/defn send-with-pubkey
  "Sends the payload using asymetric key (multiaccount `:public-key` in db) and fixed discovery topic"
  [{:keys [db] :as cofx} {:keys [payload chat-id success-event]}]
  (let [{:keys [web3]} db]
    (let [pfs? (get-in db [:multiaccount :settings :pfs?])]
      (if pfs?
        (send-direct-message cofx
                             chat-id
                             success-event
                             payload)
        (let [topic-hash             (discovery-topic-hash)]
          {:shh/post [{:web3          web3
                       :success-event success-event
                       :message       (merge {:sig     (multiaccounts.model/current-public-key cofx)
                                              :pubKey  chat-id
                                              :payload payload
                                              :topic   topic-hash}
                                             whisper-opts)}]})))))

(defrecord Message [content content-type message-type clock-value timestamp]
  StatusMessage
  (send [this chat-id {:keys [message-id] :as cofx}]
    (let [current-public-key (multiaccounts.model/current-public-key cofx)
          params             {:chat-id       chat-id
                              :payload       this
                              :success-event [:transport/message-sent
                                              chat-id
                                              message-id
                                              message-type]}]
      (case message-type
        :public-group-user-message
        (send-public-message cofx chat-id (:success-event params) this)

        :user-message
        (fx/merge cofx
                  (when (has-paired-installations? cofx)
                    (send-direct-message current-public-key nil this))
                  (send-with-pubkey params)))))
  (receive [this chat-id signature timestamp cofx]
    (let [received-message-fx {:chat-received-message/add-fx
                               [(assoc (into {} this)
                                       :old-message-id (transport.utils/old-message-id this)
                                       :message-id (transport.utils/message-id
                                                    signature
                                                    (.-payload (:js-obj cofx)))
                                       :chat-id chat-id
                                       :whisper-timestamp timestamp
                                       :raw-payload-hash (ethereum/sha3
                                                          (.-payload (:js-obj cofx)))
                                       :from signature
                                       :dedup-id (:dedup-id cofx)
                                       :js-obj (:js-obj cofx))]}]
      (whitelist/filter-message cofx
                                received-message-fx
                                message-type
                                (get-in this [:content :tribute-transaction])
                                signature)))
  (validate [this]
    (if (spec/valid? :message/message this)
      this
      (log/warn "failed to validate Message" (spec/explain-str :message/message this)))))
