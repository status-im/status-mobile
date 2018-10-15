(ns ^{:doc "Offline inboxing events and API"}
 status-im.transport.inbox
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.data-store.core :as data-store]
            [status-im.data-store.transport :as transport-store]
            [status-im.fleet.core :as fleet]
            [status-im.mailserver.core :as mailserver]
            [status-im.native-module.core :as status]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]
            [status-im.transport.db :as transport.db]))

;; How does offline inboxing work ?
;;
;; - We send a request to the mailserver, we are only interested in the
;; messages since `last-request` up to the last seven days
;; and the last 24 hours for topics that were just joined
;; - The mailserver doesn't directly respond to the request and
;; instead we start receiving messages in the filters for the requested
;; topics.
;; - If the mailserver was not ready when we tried for instance to request
;; the history of a topic after joining a chat, the request will be done
;; as soon as the mailserver becomes available


(def one-day (* 24 3600))
(def seven-days (* 7 one-day))
(def maximum-number-of-attemps 2)
(def request-timeout 30)

(def connection-timeout
  "Time after which mailserver connection is considered to have failed"
  10000)

(defn- parse-json
  ;; NOTE(dmitryn) Expects JSON response like:
  ;; {"error": "msg"} or {"result": true}
  [s]
  (try
    (let [res (-> s
                  js/JSON.parse
                  (js->clj :keywordize-keys true))]
      ;; NOTE(dmitryn): AddPeer() may return {"error": ""}
      ;; assuming empty error is a success response
      ;; by transforming {"error": ""} to {:result true}
      (if (and (:error res)
               (= (:error res) ""))
        {:result true}
        res))
    (catch :default e
      {:error (.-message e)})))

(defn- response-handler [success-fn error-fn]
  (fn handle-response
    ([response]
     (let [{:keys [error result]} (parse-json response)]
       (handle-response error result)))
    ([error result]
     (if error
       (error-fn error)
       (success-fn result)))))

(defn add-peer! [wnode]
  (status/add-peer wnode
                   (response-handler #(log/debug "offline inbox: add-peer success" %)
                                     #(log/error "offline inbox: add-peer error" %))))

(re-frame/reg-fx
 :transport.inbox/add-peer
 (fn [wnode]
   (add-peer! wnode)))

(defn mark-trusted-peer! [web3 enode]
  (.markTrustedPeer (transport.utils/shh web3)
                    enode
                    (fn [error response]
                      (if error
                        (re-frame/dispatch [:inbox.callback/mark-trusted-peer-error error])
                        (re-frame/dispatch [:inbox.callback/mark-trusted-peer-success response])))))

(re-frame/reg-fx
 :transport.inbox/mark-trusted-peer
 (fn [{:keys [wnode web3]}]
   (mark-trusted-peer! web3 wnode)))

(fx/defn generate-mailserver-symkey
  [{:keys [db] :as cofx} {:keys [password id] :as wnode}]
  (let [current-fleet (fleet/current-fleet db)]
    {:db (assoc-in db [:inbox/wnodes current-fleet id :generating-sym-key?] true)
     :shh/generate-sym-key-from-password
     [{:password    password
       :web3       (:web3 db)
       :on-success (fn [_ sym-key-id]
                     (re-frame/dispatch [:inbox.callback/generate-mailserver-symkey-success wnode sym-key-id]))
       :on-error   #(log/error "offline inbox: get-sym-key error" %)}]}))

(defn registered-peer?
  "truthy if the enode is a registered peer"
  [peers enode]
  (let [peer-ids (into #{} (map :id) peers)
        enode-id (transport.utils/extract-enode-id enode)]
    (contains? peer-ids enode-id)))

(defn update-mailserver-status [db state]
  (assoc db :mailserver-status state))

(fx/defn mark-trusted-peer
  [{:keys [db] :as cofx}]
  (let [{:keys [address sym-key-id generating-sym-key?] :as wnode} (mailserver/fetch-current cofx)]
    (fx/merge cofx
              {:db (update-mailserver-status db :added)
               :transport.inbox/mark-trusted-peer {:web3  (:web3 db)
                                                   :wnode address}}
              (when-not (or sym-key-id generating-sym-key?)
                (generate-mailserver-symkey wnode)))))

(fx/defn add-peer
  [{:keys [db] :as cofx}]
  (let [{:keys [address sym-key-id generating-sym-key?] :as wnode} (mailserver/fetch-current cofx)]
    (fx/merge cofx
              {:db (-> db
                       (update-mailserver-status :connecting)
                       (update :transport.inbox/connection-checks inc))
               :transport.inbox/add-peer address
               :utils/dispatch-later [{:ms connection-timeout
                                       :dispatch [:inbox/check-connection-timeout]}]}
              (when-not (or sym-key-id generating-sym-key?)
                (generate-mailserver-symkey wnode)))))

(fx/defn connect-to-mailserver
  "Add mailserver as a peer using `::add-peer` cofx and generate sym-key when
   it doesn't exists
   Peer summary will change and we will receive a signal from status go when
   this is successful
   A connection-check is made after `connection timeout` is reached and
   mailserver-status is changed to error if it is not connected by then"
  [{:keys [db] :as cofx}]
  (let [{:keys [address] :as wnode} (mailserver/fetch-current cofx)
        {:keys [peers-summary]} db
        added? (registered-peer? peers-summary
                                 address)]
    (fx/merge cofx
              {:db (dissoc db :transport.inbox/current-request)}
              (if added?
                (mark-trusted-peer)
                (add-peer)))))

(fx/defn peers-summary-change
  "There is only 2 summary changes that require offline inboxing action:
  - mailserver disconnected: we try to reconnect
  - mailserver connected: we mark the mailserver as trusted peer"
  [{:keys [db] :as cofx} previous-summary]
  (when (:account/account db)
    (let [{:keys [peers-summary peers-count]} db
          {:keys [address sym-key-id] :as wnode} (mailserver/fetch-current cofx)
          mailserver-was-registered? (registered-peer? previous-summary
                                                       address)
          mailserver-is-registered?  (registered-peer? peers-summary
                                                       address)
          mailserver-added?          (and mailserver-is-registered?
                                          (not mailserver-was-registered?))
          mailserver-removed?        (and mailserver-was-registered?
                                          (not mailserver-is-registered?))]
      (cond
        mailserver-added?
        (mark-trusted-peer cofx)
        mailserver-removed?
        (connect-to-mailserver cofx)))))

(defn request-messages! [web3 {:keys [sym-key-id address]} {:keys [topic to from]}]
  (log/info "offline inbox: request-messages for: "
            " topic " topic
            " from " from
            " to   " to)
  (.requestMessages (transport.utils/shh web3)
                    (clj->js {:topic          topic
                              :mailServerPeer address
                              :symKeyID       sym-key-id
                              :timeout        request-timeout
                              :from           from
                              :to             to})
                    (fn [error request-id]
                      (if-not error
                        (log/info "offline inbox: messages request success for topic " topic "from" from "to" to)
                        (log/error "offline inbox: messages request error for topic " topic ": " error)))))

(re-frame/reg-fx
 :transport.inbox/request-messages
 (fn [{:keys [web3 wnode request]}]
   (request-messages! web3 wnode request)))

(defn get-wnode-when-ready
  "return the wnode if the inbox is ready"
  [{:keys [db] :as cofx}]
  (let [{:keys [sym-key-id] :as wnode} (mailserver/fetch-current cofx)
        mailserver-status (:mailserver-status db)]
    (when (and (= :connected mailserver-status)
               sym-key-id)
      wnode)))

(defn split-request-per-day
  "NOTE: currently the mailserver is only accepting requests for a span
  of 24 hours, so we split requests per 24h spans if the last request was
  done more than 24h ago"
  [now-in-s [topic {:keys [last-request]}]]
  (let [days        (conj
                     (into [] (range (max last-request
                                          (- now-in-s seven-days))
                                     now-in-s
                                     one-day))
                     now-in-s)
        day-ranges  (map vector days (rest days))]
    (for [[from to] day-ranges]
      {:topic topic
       :from  from
       :to    to})))

(defn prepare-messages-requests
  [{:keys [db now] :as cofx} request-to]
  (let [web3     (:web3 db)]
    (remove nil?
            (mapcat (partial split-request-per-day request-to)
                    (:transport.inbox/topics db)))))

(fx/defn process-next-messages-request
  [{:keys [db now] :as cofx}]
  (when (and (transport.db/all-filters-added? cofx)
             (not (:transport.inbox/current-request db)))
    (when-let [wnode (get-wnode-when-ready cofx)]
      (let [request-to (or (:transport.inbox/request-to db)
                           (quot now 1000))
            requests (prepare-messages-requests cofx request-to)
            web3 (:web3 db)]
        (if-let [request (first requests)]
          {:db (assoc db
                      :transport.inbox/pending-requests (count requests)
                      :transport.inbox/current-request request
                      :transport.inbox/request-to request-to)
           :transport.inbox/request-messages {:web3     web3
                                              :wnode    wnode
                                              :request request}}
          {:db (dissoc db
                       :transport.inbox/pending-requests
                       :transport.inbox/request-to)})))))

(fx/defn add-mailserver-trusted
  "the current mailserver has been trusted
  update mailserver status to `:connected` and request messages
  if wnode is ready"
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update-mailserver-status db :connected)}
            (process-next-messages-request)))

(fx/defn add-mailserver-sym-key
  "the current mailserver sym-key has been generated
  add sym-key to the wnode in app-db and request messages if
  wnode is ready"
  [{:keys [db] :as cofx} {:keys [id]} sym-key-id]
  (let [current-fleet (fleet/current-fleet db)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:inbox/wnodes current-fleet id :sym-key-id] sym-key-id)
                       (update-in [:inbox/wnodes current-fleet id] dissoc :generating-sym-key?))}
              (process-next-messages-request))))

(fx/defn change-mailserver
  "mark mailserver status as `:error` if custom mailserver is used
  otherwise try to reconnect to another mailserver"
  [{:keys [db] :as cofx}]
  (if (mailserver/preferred-mailserver-id cofx)
    {:db (update-mailserver-status db :error)}
    (fx/merge cofx
              (mailserver/set-current-mailserver)
              (connect-to-mailserver))))

(fx/defn check-connection
  "connection-checks counter is used to prevent changing
   mailserver on flaky connections
   if there is more than one connection check pending
      decrement the connection check counter
   else
      change mailserver if mailserver is connected"
  [{:keys [db] :as cofx}]
  (if (zero? (dec (:transport.inbox/connection-checks db)))
    (fx/merge cofx
              {:db (dissoc db :transport.inbox/connection-checks)}
              (when (= :connecting (:mailserver-status db))
                (change-mailserver cofx)))
    {:db (update db :transport.inbox/connection-checks dec)}))

(fx/defn reset-request-to
  [{:keys [db]}]
  {:db (dissoc db :transport.inbox/request-to)})

(fx/defn network-connection-status-changed
  "when host reconnects, reset request-to and
  reconnect to mailserver"
  [{:keys [db] :as cofx} is-connected?]
  (when (and (accounts.db/logged-in? cofx)
             is-connected?)
    (fx/merge cofx
              (reset-request-to)
              (connect-to-mailserver))))

(fx/defn remove-chat-from-inbox-topic
  "if the chat is the only chat of the inbox topic delete the inbox topic
   and process-next-messages-requests again to remove pending request for that topic
   otherwise remove the chat-id of the chat from the inbox topic and save"
  [{:keys [db now] :as cofx} chat-id]
  (let [topic (get-in db [:transport/chats chat-id :topic])
        {:keys [chat-ids] :as inbox-topic} (update (get-in db [:transport.inbox/topics topic])
                                                   :chat-ids
                                                   disj chat-id)]
    (if (empty? chat-ids)
      (fx/merge cofx
                {:db (update db :transport.inbox/topics dissoc topic)
                 :data-store/tx [(transport-store/delete-transport-inbox-topic-tx topic)]}
                (process-next-messages-request))
      {:db (assoc-in db [:transport.inbox/topics topic] inbox-topic)
       :data-store/tx [(transport-store/save-transport-inbox-topic-tx
                        {:topic topic
                         :inbox-topic inbox-topic})]})))

(fx/defn update-inbox-topic
  "TODO: add support for cursors
  if there is a cursor, do not update `last-request`"
  [{:keys [db now] :as cofx} {:keys [request-id cursor]}]
  (when-let [request (get db :transport.inbox/current-request)]
    (let [{:keys [from to topic]} request
          inbox-topic (some-> (get-in db [:transport.inbox/topics topic])
                              (assoc :last-request to))]
      (log/info "offline inbox: message request " request-id
                "completed for inbox topic" topic "from" from "to" to)
      (if inbox-topic
        (fx/merge cofx
                  (if inbox-topic
                    {:db (-> db
                             (dissoc :transport.inbox/current-request)
                             (assoc-in [:transport.inbox/topics topic] inbox-topic))
                     :data-store/tx [(transport-store/save-transport-inbox-topic-tx
                                      {:topic topic
                                       :inbox-topic inbox-topic})]})
                  (process-next-messages-request))
        ;; when the topic was deleted (filter was removed while request was pending)
        (fx/merge cofx
                  {:db (dissoc db :transport.inbox/current-request)}
                  (process-next-messages-request))))))

(fx/defn upsert-inbox-topic
  "if the topic didn't exist
      create the topic
   else if chat-id is not in the topic
      add the chat-id to the topic and reset last-request
      there was no filter for the chat and messages for that
      so the whole history for that topic needs to be re-fetched"
  [{:keys [db] :as cofx} {:keys [topic chat-id]}]
  (let [{:keys [chat-ids last-request] :as current-inbox-topic}
        (get-in db [:transport.inbox/topics topic] {:chat-ids #{}})]
    (when-let [inbox-topic (when-not (chat-ids chat-id)
                             (-> current-inbox-topic
                                 (assoc :last-request 1)
                                 (update :chat-ids conj chat-id)))]
      (fx/merge cofx
                {:db (assoc-in db [:transport.inbox/topics topic] inbox-topic)
                 :data-store/tx [(transport-store/save-transport-inbox-topic-tx
                                  {:topic topic
                                   :inbox-topic inbox-topic})]}))))

(fx/defn resend-request
  [{:keys [db] :as cofx} {:keys [request-id]}]
  (if (<= maximum-number-of-attemps
          (get-in db [:transport.inbox/current-request :attemps]))
    (fx/merge cofx
              {:db (update db :transport.inbox/current-request dissoc :attemps)}
              (change-mailserver))
    (when-let [wnode (get-wnode-when-ready cofx)]
      (let [{:keys [topic from to] :as request} (get db :transport.inbox/current-request)
            web3 (:web3 db)]
        (log/info "offline inbox: message request " request-id "expired for inbox topic" topic "from" from "to" to)
        {:db (update-in db [:transport.inbox/current-request :attemps] inc)
         :transport.inbox/request-messages {:web3    web3
                                            :wnode   wnode
                                            :request request}}))))

(fx/defn initialize-offline-inbox
  [cofx custom-mailservers]
  (fx/merge cofx
            (mailserver/add-custom-mailservers custom-mailservers)
            (mailserver/set-current-mailserver)))
