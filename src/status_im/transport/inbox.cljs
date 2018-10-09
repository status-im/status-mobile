(ns ^{:doc "Offline inboxing events and API"}
 status-im.transport.inbox
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.accounts :as accounts-store]
            [status-im.data-store.core :as data-store]
            [status-im.data-store.transport :as transport-store]
            [status-im.fleet.core :as fleet]
            [status-im.mailserver.core :as mailserver]
            [status-im.native-module.core :as status]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

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

(def connection-timeout
  "Time after which mailserver connection is considered to have failed"
  15000)

(def fetching-timeout
  "Time we should wait after last message was fetch from mailserver before we
   consider it done
   Needs to be at least 10 seconds because that is the time it takes for the app
   to realize it was disconnected"
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
 :inbox/add-peer
 (fn [wnode]
   (add-peer! wnode)))

(defn mark-trusted-peer! [web3 enode success-fn error-fn]
  (.markTrustedPeer (transport.utils/shh web3)
                    wnode
                    (fn [err resp]
                      (if-not err
                        #(re-frame/dispatch [:inbox.callback/mark-trusted-peer-success resp])
                        #(re-frame/dispatch [:inbox.callback/mark-trusted-peer-error error])))))

(re-frame/reg-fx
 :inbox/mark-trusted-peer
 (fn [{:keys [wnode web3]}]
   (mark-trusted-peer! web3 wnode)))

(fx/defn generate-mailserver-symkey
  [{:keys [db] :as cofx} wnode]
  {:shh/generate-sym-key-from-password
   [{:password   (:password wnode)
     :web3       (:web3 db)
     :on-success (fn [_ sym-key-id]
                   (re-frame/dispatch [:inbox.callback/generate-mailserver-symkey-success wnode sym-key-id]))
     :on-error   #(log/error "offline inbox: get-sym-key error" %)}]})

(defn registered-peer?
  "truthy if the enode is a registered peer"
  [peers enode]
  (let [peer-ids (into #{} (map :id) peers)
        enode-id (transport.utils/extract-enode-id enode)]
    (contains? peer-ids enode-id)))

(defn update-mailserver-status [db state]
  (assoc db :mailserver-status state))

(fx/defn connect-to-mailserver
  "Add mailserver as a peer using `::add-peer` cofx and generate sym-key when
   it doesn't exists
   Peer summary will change and we will receive a signal from status go when
   this is successful
   A connection-check is made after `connection timeout` is reached and
   mailserver-status is changed to error if it is not connected by then"
  [{:keys [db] :as cofx}]
  (let [{:keys [address sym-key-id] :as wnode} (mailserver/fetch-current cofx)
        {:keys [web3]} db]
    (fx/merge cofx
              {:db (update-mailserver-status db :connecting)
               :inbox/add-peer address
               :utils/dispatch-later [{:ms connection-timeout
                                       :dispatch [:inbox/check-connection]}]}
              (when-not sym-key-id
                (generate-mailserver-symkey wnode)))))

(fx/defn peers-summary-change
  "There is only 2 summary changes that require offline inboxing action:
  - mailserver disconnected: we try to reconnect
  - mailserver connected: we mark the mailserver as trusted peer"
  [{:keys [db] :as cofx} previous-summary]
  (when (:account/account db)
    (let [{:keys [peers-summary peers-count]} db
          {:keys [address sym-key-id] :as wnode} (:address (mailserver/fetch-current cofx))
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
        (fx/merge cofx
                  {:db (update-mailserver-status db :added)
                   :inbox/mark-trusted-peer {:web3  (:web3 db)
                                             :wnode address}}
                  (when-not sym-key-id
                    (generate-mailserver-symkey wnode)))
        mailserver-removed?
        (fx/merge cofx
                  {:db (update-mailserver-status :disconnected)}
                  (connect-to-mailserver))))))

(defn request-messages! [web3 wnode {:keys [topic to from]}]
  (log/info "offline inbox: request-messages for: "
            " topic " topic
            " from " from
            " to   " to)
  (.requestMessages (transport.utils/shh web3)
                    (clj->js {:topic          topic
                              :mailServerPeer address
                              :symKeyID       sym-key-id
                              :from           from
                              :to             to})
                    (fn [err request-id]
                      (if-not err
                        #(re-frame/dispatch [:inbox.callback/request-messages-success {:topic      topic
                                                                                       :request-id request-id
                                                                                       :from       from
                                                                                       :to         to}])
                        #(log/error "offline inbox: messages request error for topic " topic ": " err)))))

(re-frame/reg-fx
 :inbox/request-messages
 (fn [web3 wnode requests]
   (doseq [{:keys [web3 wnode request]} requests]
     (request-messages! web3 wnode request))))

(defn prepare-request [now-in-s [chat-id {:keys [request-from] :as chat}]]
  {:from  (max request-from
               (- now-in-s seven-days))
   :to    now-in-s
   :topic topic
   :chat-id chat-id})

(defn get-wnode-when-ready
  "return the wnode if the inbox is ready"
  [{:keys [db]}]
  (let [{:keys [sym-key-id]} (mailserver/fetch-current cofx)
        mailserver-status (:mailserver-status db)]
    (when (and (= :connected mailserver-status)
               sym-key-id)
      wnode)))

(fx/defn request-messages
  "request messages if the inbox is ready"
  [{:keys [db now] :as cofx} chat-id]
  (when-let [wnode (get-wnode-when-ready cofx)]
    (let [web3     (:web3 db)
          now-in-s (quot now 1000)
          requests (map (prepare-request now-in-s)
                        (if chat-id
                          [chat-id (get-in db [:transport/inbox-topics chat-id])]
                          (:transport/inbox-topics db)))]
      {:inbox/request-messages [web3 wnode requests]})))

(fx/defn add-mailserver-trusted
  "the current mailserver has been trusted
  update mailserver status to `:connected` and request messages
  if wnode is ready"
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update-mailserver-status db :connected)}
            (request-messages nil)))

(fx/defn add-mailserver-sym-key
  "the current mailserver sym-key has been generated
  add sym-key to the wnode in app-db and request messages if
  wnode is ready"
  [{:keys [db] :as cofx} {:keys [id]} sym-key-id]
  (let [current-fleet (fleet/current-fleet db)]
    (fx/merge cofx
              {:db (assoc-in db [:inbox/wnodes current-fleet id :sym-key-id] sym-key-id)}
              (request-messages nil))))

(fx/defn check-connection
  "check if mailserver is connected
   mark mailserver status as `:error` if custom mailserver is used
   otherwise try to reconnect to another mailserver"
  [{:keys [db] :as cofx}]
  (when (= :connecting (:mailserver-status db))
    (if (mailserver/preferred-mailserver-id cofx)
      {:db (update-mailserver-status db :error)}
      (fx/merge cofx
                (mailserver/set-current-mailserver)
                (connect-to-mailserver)))))

(fx/defn update-inbox-topic
  "TODO: add support for cursors
  if there is a cursor, do not update `request-to` and `request-from`"
  [{:keys [db now] :as cofx} {:keys [request-id cursor]}]
  (let [now-in-s       (quot now 1000)
        {:keys [from to topic]} (get-in db [:transport.inbox/requests request-id])]
    (fx/merge cofx
              {:db (-> db
                       (update :transport.inbox/requests dissoc request-id)
                       (assoc-in db [:transport.inbox/topic topic :last-request] from))
               :data-store/tx [(transport-store/save-transport-inbox-topic-tx
                                {:topic topic
                                 :last-request from})]})))

(fx/defn add-request
  [{:keys [db]} {:keys [inbox-topic request-id from to]}]
  (log/info "offline inbox: message request sent for inbox topic" inbox-topic)
  {:db (assoc-in db [:transport.inbox/requests request-id] {:from from
                                                            :to to
                                                            :topic inbox-topic})})

(fx/defn initialize-offline-inbox [cofx custom-mailservers]
  (fx/merge cofx
            (mailserver/add-custom-mailservers custom-mailservers)
            (mailserver/set-current-mailserver)))

;; HANDLERS

(handlers/register-handler-fx
 :inbox.callback/mark-trusted-peer-success
 (fn [cofx _]
   (add-mailserver-trusted cofx)))

(handlers/register-handler-fx
 :inbox.callback/mark-trusted-peer-error
 (fn [cofx [_ error]]
   (log/error "Error on mark-trusted-peer: " error)
   (check-connection cofx)))

(handlers/register-handler-fx
 :inbox.ui/reconnect-mailserver-pressed
 (fn [cofx [_ args]]
   (connect-to-mailserver cofx)))

(handlers/register-handler-fx
 :inbox/check-connection
 (fn [cofx _]
   (check-connection cofx)))

(handlers/register-handler-fx
 :inbox.callback/generate-mailserver-symkey-success
 (fn [cofx [_ wnode sym-key-id]]
   (add-mailserver-sym-key cofx wnode sym-key-id)))

(re-frame/register-handler
 :inbox.callback/request-messages-success
 (fn [cofx [_ request]]
   (add-request cofx request)))
