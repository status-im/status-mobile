(ns ^{:doc "Offline inboxing events and API"}
 status-im.transport.inbox
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]
            [status-im.constants :as constants]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]
            [status-im.constants :as constants]
            [status-im.data-store.accounts :as accounts-store]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.data-store.core :as data-store]
            [status-im.models.mailserver :as models.mailserver]
            [status-im.data-store.transport :as transport-store]))

;; How does offline inboxing work ?
;;
;; - We send a request to the mailserver, we are only interested in the
;; messages since `last-request`, the time of the last successful request,
;; and the last 24 hours for topics that were just joined
;; - The mailserver doesn't directly respond to the request and
;; instead we start receiving messages in the filters for the requested
;; topics.
;; - These messages are expired that is how we differentiate them from
;; normal whisper messages to update last-received
;; - After fetching-timeout is reached since the last mailserver message
;; was received without a connection incident, we consider the request
;; successfull and update `last-request` and `fetch-history?` fields of each
;; topic to false
;; - If the mailserver was not ready when we tried for instance to request
;; the history of a topic after joining a chat, the request will be done
;; as soon as the mailserver becomes available

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

(defn- response-handler [error-fn success-fn]
  (fn handle-response
    ([response]
     (let [{:keys [error result]} (parse-json response)]
       (handle-response error result)))
    ([error result]
     (if error
       (error-fn error)
       (success-fn result)))))

(defn add-sym-key-id-to-wnode [{:keys [id]} sym-key-id {:keys [db]}]
  (let [network  (get (:networks (:account/account db)) (:network db))
        chain    (ethereum/network->chain-keyword network)]
    {:db (assoc-in db [:inbox/wnodes chain id :sym-key-id] sym-key-id)}))

(defn registered-peer? [peers enode]
  (let [peer-ids (into #{} (map :id) peers)
        enode-id (transport.utils/extract-enode-id enode)]
    (contains? peer-ids enode-id)))

(defn add-peer [enode success-fn error-fn]
  (status/add-peer enode (response-handler error-fn success-fn)))

(defn mark-trusted-peer [web3 enode success-fn error-fn]
  (.markTrustedPeer (transport.utils/shh web3)
                    enode
                    (fn [err resp]
                      (if-not err
                        (success-fn resp)
                        (error-fn err)))))

(def one-day (* 24 3600))
(def seven-days (* 7 one-day))

(defn request-inbox-messages-params [mailserver from to topics]
  (let [days        (conj
                     (into [] (range from to one-day))
                     to)
        day-ranges  (map vector days (drop 1 days))]
    (for [topic topics
          [current-from current-to] day-ranges]
      {:topic          topic
       :mailServerPeer (:address mailserver)
       :symKeyID       (:sym-key-id mailserver)
       :from           current-from
       :to             current-to})))

(defn request-inbox-messages
  [web3 mailserver topics start-from end-to success-fn error-fn]
  (log/info "offline inbox: request-messages request for topics " topics " from " start-from " to " end-to)
  (doseq [{:keys [topic] :as params} (request-inbox-messages-params
                                      mailserver
                                      start-from
                                      end-to
                                      topics)]
    (log/info "offline inbox: request-messages for: "
              " topic " topic
              " from "  (:from params)
              " to   "  (:to params))
    (.requestMessages (transport.utils/shh web3)
                      (clj->js params)
                      (fn [err resp]
                        (if-not err
                          (success-fn resp topic)
                          (error-fn err topic))))))

(re-frame/reg-fx
 ::add-peer
 (fn [{:keys [wnode]}]
   (add-peer wnode
             #(log/debug "offline inbox: add-peer success" %)
             #(log/error "offline inbox: add-peer error" %))))

(re-frame/reg-fx
 ::mark-trusted-peer
 (fn [{:keys [wnode web3]}]
   (mark-trusted-peer web3
                      wnode
                      #(re-frame/dispatch [:inbox/mailserver-trusted %])
                      #(re-frame/dispatch [:inbox/check-connection]))))

(re-frame/reg-fx
 ::request-messages
 (fn [params]
   (doseq [{:keys [wnode topics to from web3]} params]
     (request-inbox-messages web3
                             wnode
                             topics
                             from
                             to
                             #(log/info "offline inbox: request-messages response" %1 %2 from to)
                             #(log/error "offline inbox: request-messages error" %1 %2 from to)))))

(defn update-mailserver-status [transition {:keys [db]}]
  (let [state transition]
    {:db (assoc db
                :mailserver-status state
                :inbox/fetching? false)}))

(defn generate-mailserver-symkey [wnode {:keys [db] :as cofx}]
  (when-not (:sym-key-id wnode)
    {:shh/generate-sym-key-from-password
     {:password   (:password wnode)
      :web3       (:web3 db)
      :on-success (fn [_ sym-key-id]
                    (re-frame/dispatch [:inbox/get-sym-key-success wnode sym-key-id]))
      :on-error   #(log/error "offline inbox: get-sym-key error" %)}}))

(defn connect-to-mailserver
  "Add mailserver as a peer using ::add-peer cofx and generate sym-key when
   it doesn't exists
   Peer summary will change and we will receive a signal from status go when
   this is successful
   A connection-check is made after `connection timeout` is reached and
   mailserver-status is changed to error if it is not connected by then"
  [{:keys [db] :as cofx}]
  (let [web3                        (:web3 db)
        {:keys [address] :as wnode} (models.mailserver/fetch-current cofx)
        peers-summary               (:peers-summary db)
        connected?                  (registered-peer? peers-summary address)]
    (if connected?
      (handlers-macro/merge-fx cofx
                               (update-mailserver-status :connected)
                               (generate-mailserver-symkey wnode))
      (handlers-macro/merge-fx cofx
                               {::add-peer {:wnode address}
                                :utils/dispatch-later [{:ms connection-timeout
                                                        :dispatch [:inbox/check-connection]}]}
                               (update-mailserver-status :connecting)
                               (generate-mailserver-symkey wnode)))))

(defn peers-summary-change-fx
  "There is only 2 summary changes that require offline inboxing action:
  - mailserver disconnected: we try to reconnect
  - mailserver connected: we mark the mailserver as trusted peer"
  [previous-summary {:keys [db] :as cofx}]
  (when (:account/account db)
    (let [{:keys [peers-summary peers-count]} db
          wnode                               (:address (models.mailserver/fetch-current cofx))
          mailserver-was-registered?          (registered-peer? previous-summary
                                                                wnode)
          mailserver-is-registered?           (registered-peer? peers-summary
                                                                wnode)
          ;; the mailserver just connected
          mailserver-connected?               (and mailserver-is-registered?
                                                   (not mailserver-was-registered?))
          ;; the mailserver just disconnected
          mailserver-disconnected?            (and mailserver-was-registered?
                                                   (not mailserver-is-registered?))]
      (cond
        mailserver-disconnected?
        (connect-to-mailserver cofx)

        mailserver-connected?
        {::mark-trusted-peer {:web3  (:web3 db)
                              :wnode wnode}}))))

(defn inbox-ready? [{:keys [sym-key-id]} {:keys [db]}]
  (let [mailserver-status (:mailserver-status db)]
    (and (= :connected mailserver-status)
         sym-key-id)))

(defn get-request-messages-topics
  "Returns topics for which full history has already been recovered"
  [db]
  (conj (map :topic
             (remove :fetch-history?
                     (vals (:transport/chats db))))
        (transport.utils/get-topic constants/contact-discovery)))

(defn get-request-history-topics
  "Returns topics for which full history has not been recovered"
  [db]
  (map :topic
       (filter :fetch-history?
               (vals (:transport/chats db)))))

(defn request-history-span [now-in-s]
  (- now-in-s one-day))

(defn request-messages
  ([{:keys [db now] :as cofx}]
   (let [wnode                   (models.mailserver/fetch-current cofx)
         web3                    (:web3 db)
         now-in-s                (quot now 1000)
         last-request            (max
                                  (get-in db [:account/account :last-request])
                                  (- now-in-s seven-days))
         request-messages-topics (get-request-messages-topics db)
         request-history-topics  (get-request-history-topics db)]
     (when (inbox-ready? wnode cofx)
       {::request-messages [{:wnode      wnode
                             :topics     request-messages-topics
                             :from       last-request
                             :to         now-in-s
                             :web3       web3}
                            {:wnode      wnode
                             :from       (request-history-span now-in-s)
                             :to         now-in-s
                             :topics     request-history-topics
                             :web3       web3}]
        :db                (assoc db :inbox/fetching? true)
        :dispatch-later    [{:ms fetching-timeout
                             :dispatch [:inbox/check-fetching now-in-s]}]})))
  ([should-recover? {:keys [db] :as cofx}]
   (when should-recover?
     (request-messages cofx))))

(defn request-chat-history [chat-id {:keys [db now] :as cofx}]
  (let [wnode             (models.mailserver/fetch-current cofx)
        web3              (:web3 db)
        topic             (get-in db [:transport/chats chat-id :topic])
        now-in-s          (quot now 1000)]
    (when (inbox-ready? wnode cofx)
      {::request-messages [{:wnode      wnode
                            :topics     [topic]
                            :from       (request-history-span now-in-s)
                            :to         now-in-s
                            :web3       web3}]
       :db                (assoc db :inbox/fetching? true)
       :dispatch-later    [{:ms fetching-timeout
                            :dispatch [:inbox/check-fetching now-in-s chat-id]}]})))

;;;; Handlers

(handlers/register-handler-fx
 :inbox/mailserver-trusted
 (fn [{:keys [db] :as cofx} _]
   (handlers-macro/merge-fx cofx
                            (update-mailserver-status :connected)
                            (request-messages))))

(handlers/register-handler-fx
 :inbox/get-sym-key-success
 (fn [{:keys [db] :as cofx} [_ wnode sym-key-id]]
   (handlers-macro/merge-fx cofx
                            (add-sym-key-id-to-wnode wnode sym-key-id)
                            (request-messages))))

(handlers/register-handler-fx
 :inbox/request-chat-history
 (fn [{:keys [db] :as cofx} [_ chat-id]]
   (request-chat-history chat-id cofx)))

(handlers/register-handler-fx
 :inbox/check-connection
 (fn [{:keys [db] :as cofx} _]
   (when (= :connecting (:mailserver-status db))
     (if (models.mailserver/preferred-mailserver-id cofx)
       (update-mailserver-status :error cofx)
       (handlers-macro/merge-fx cofx
                                (models.mailserver/set-current-mailserver)
                                (connect-to-mailserver))))))

(defn update-last-request [last-request {:keys [db]}]
  (let [chats         (:transport/chats db)
        transport-txs (reduce (fn [txs [chat-id chat]]
                                (if (:fetch-history? chat)
                                  (conj txs
                                        (transport-store/save-transport-tx
                                         {:chat-id chat-id
                                          :chat    (assoc chat
                                                          :fetch-history? false)}))
                                  txs))
                              []
                              chats)
        chats-update  (reduce (fn [acc [chat-id chat]]
                                (if (:fetch-history? chat)
                                  (assoc acc chat-id (assoc chat :fetch-history? false))
                                  (assoc acc chat-id chat)))
                              {}
                              chats)]
    {:db                 (-> db
                             (assoc :transport/chats chats-update)
                             (assoc-in [:account/account :last-request]
                                       last-request))
     :data-store/base-tx [(accounts-store/save-account-tx
                           (assoc (:account/account db)
                                  :last-request last-request))]
     :data-store/tx      transport-txs}))

(defn update-fetch-history [chat-id {:keys [db]}]
  {:db            (assoc-in db
                            [:transport/chats chat-id :fetch-history?]
                            false)
   :data-store/tx [(transport-store/save-transport-tx
                    {:chat-id chat-id
                     :chat (assoc (get-in db [:transport/chats chat-id])
                                  :fetch-history? false)})]})

(defn initialize-offline-inbox [custom-mailservers cofx]
  (handlers-macro/merge-fx cofx
                           (models.mailserver/add-custom-mailservers custom-mailservers)
                           (models.mailserver/set-initial-last-request)
                           (models.mailserver/set-current-mailserver)))

(handlers/register-handler-fx
 :inbox/check-fetching
 (fn [{:keys [db now] :as cofx} [_ last-request chat-id]]
   (when (and (:inbox/fetching? db)
              ;; if chat was removed before messages were fetched no need
              ;; to proceed with further actions
              (or (not chat-id) (contains? (:transport/chats db) chat-id)))
     (let [time-since-last-received (- now (:inbox/last-received db))]
       (if (> time-since-last-received fetching-timeout)
         (if chat-id
           (handlers-macro/merge-fx cofx
                                    {:db (assoc db :inbox/fetching? false)}
                                    (update-fetch-history chat-id))
           (handlers-macro/merge-fx cofx
                                    {:db (assoc db :inbox/fetching? false)}
                                    (update-last-request last-request)))
         {:dispatch-later [{:ms       (- fetching-timeout
                                         time-since-last-received)
                            :dispatch [:inbox/check-fetching last-request chat-id]}]})))))

(handlers/register-handler-fx
 :inbox/reconnect
 (fn [cofx [_ args]]
   (connect-to-mailserver cofx)))
