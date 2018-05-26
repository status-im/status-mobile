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
            [status-im.utils.handlers-macro :as handlers-macro]))

(def connection-timeout
  "Time after which mailserver connection is considered to have failed"
  60000)

(def fetching-messages-notification-timeout
  "Time after which we consider mailserver is done fetching messages and we can
  stop showing notification to user"
  5000)

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

(defn get-current-wnode-address [db]
  (let [network  (get (:networks (:account/account db)) (:network db))
        chain    (ethereum/network->chain-keyword network)
        wnode-id (get-in db [:account/account :settings :wnode chain])]
    (get-in db [:inbox/wnodes chain wnode-id :address])))

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

(defn request-inbox-messages
  [web3 wnode topics to from sym-key-id success-fn error-fn]
  (let [opts (merge {:mailServerPeer wnode
                     :symKeyID       sym-key-id}
                    (when from {:from from})
                    (when to {:to to}))]
    (log/info "offline inbox: request-messages request for topics " topics)
    (doseq [topic topics]
      (let [opts (assoc opts :topic topic)]
        (.requestMessages (transport.utils/shh web3)
                          (clj->js opts)
                          (fn [err resp]
                            (if-not err
                              (success-fn resp)
                              (error-fn err topic))))))))

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
                      #(re-frame/dispatch [:inbox/connection-check]))))

(re-frame/reg-fx
 ::request-messages
 (fn [{:keys [wnode topics to from sym-key-id web3]}]
   (request-inbox-messages web3
                           wnode
                           topics
                           to
                           from
                           sym-key-id
                           #(log/info "offline inbox: request-messages response" %)
                           #(log/error "offline inbox: request-messages error" %1 %2 to from))))

(defn update-mailserver-status [transition {:keys [db]}]
  (let [state transition]
    {:db (assoc db :mailserver-status state)}))

(defn generate-mailserver-symkey [{:keys [db] :as cofx}]
  (when-not (:inbox/sym-key-id db)
    {:shh/generate-sym-key-from-password
     {:password   (:inbox/password db)
      :web3       (:web3 db)
      :on-success (fn [_ sym-key-id]
                    (re-frame/dispatch [:inbox/get-sym-key-success sym-key-id]))
      :on-error   #(log/error "offline inbox: get-sym-key error" %)}}))

(defn connect-to-mailserver
  "Add mailserver as a peer using ::add-peer cofx and generate sym-key when
   it doesn't exists
   Peer summary will change and we will receive a signal from status go when
   this is successful
   A connection-check is made after `connection timeout` is reached and
   mailserver-status is changed to error if it is not connected by then"
  [{:keys [db] :as cofx}]
  (let [web3          (:web3 db)
        wnode         (get-current-wnode-address db)
        peers-summary (:peers-summary db)
        connected?    (registered-peer? peers-summary wnode)]
    (when config/offline-inbox-enabled?
      (if connected?
        (handlers-macro/merge-fx cofx
                                 (update-mailserver-status :connected)
                                 (generate-mailserver-symkey))
        (handlers-macro/merge-fx cofx
                                 {::add-peer {:wnode wnode}
                                  :utils/dispatch-later [{:ms connection-timeout
                                                          :dispatch [:inbox/connection-check]}]}
                                 (update-mailserver-status :connecting)
                                 (generate-mailserver-symkey))))))

(defn peers-summary-change-fx
  "There is only 2 summary changes that require offline inboxing action:
  - mailserver disconnected: we try to reconnect
  - mailserver connected: we mark the mailserver as trusted peer"
  [previous-summary {:keys [db] :as cofx}]
  (when (and (:account/account db)
             config/offline-inbox-enabled?)
    (let [{:keys [peers-summary peers-count]} db
          wnode                               (get-current-wnode-address db)
          mailserver-was-registered?          (registered-peer? previous-summary
                                                                wnode)
          mailserver-is-registered?           (registered-peer? peers-summary
                                                                wnode)
          mailserver-connected?               (and mailserver-is-registered?
                                                   (not mailserver-was-registered?))
          mailserver-disconnected?            (and mailserver-was-registered?
                                                   (not mailserver-is-registered?))]
      (cond
        mailserver-disconnected?
        (connect-to-mailserver cofx)

        mailserver-connected?
        {::mark-trusted-peer {:web3  (:web3 db)
                              :wnode wnode}}))))

(defn get-topics
  [db topics discover?]
  (let [inbox-topics    (:inbox/topics db)
        discovery-topic (transport.utils/get-topic constants/contact-discovery)
        topics          (or topics
                            (map #(:topic %) (vals (:transport/chats db))))]
    (cond-> (apply conj inbox-topics topics)
      discover? (conj discovery-topic))))

(defn request-messages
  ([cofx]
   (request-messages {} cofx))
  ([{:keys [topics discover? should-recover?]
     :or {should-recover? true
          discover?       true}}
    {:keys [db] :as cofx}]
   (let [mailserver-status (:mailserver-status db)
         sym-key-id        (:inbox/sym-key-id db)
         wnode             (get-current-wnode-address db)
         inbox-topics      (get-topics db topics discover?)
         inbox-ready?      (and (= :connected mailserver-status)
                                sym-key-id)]
     (when should-recover?
       (if inbox-ready?
         {::request-messages {:wnode      wnode
                              :topics     (into [] inbox-topics)
                              :sym-key-id sym-key-id
                              :web3       (:web3 db)}
          :db                (assoc db
                                    :inbox/fetching? true
                                    :inbox/topics #{})
          :dispatch-later    [{:ms fetching-messages-notification-timeout
                               :dispatch [:inbox/remove-fetching-notification]}]}
         {:db (assoc db :inbox/topics (into #{} inbox-topics))})))))

;;;; Handlers

(handlers/register-handler-fx
 :inbox/mailserver-trusted
 (fn [{:keys [db] :as cofx} _]
   (handlers-macro/merge-fx cofx
                            (update-mailserver-status :connected)
                            (request-messages))))

(defn add-custom-mailservers [mailservers {:keys [db]}]
  {:db (reduce (fn [db {:keys [id chain] :as mailserver}]
                 (assoc-in db [:inbox/wnodes (keyword chain) id]
                           (-> mailserver
                               (dissoc :chain)
                               (assoc :user-defined true))))
               db
               mailservers)})

(handlers/register-handler-fx
 :inbox/get-sym-key-success
 (fn [{:keys [db] :as cofx} [_ sym-key-id]]
   (handlers-macro/merge-fx cofx
                            {:db (assoc db :inbox/sym-key-id sym-key-id)}
                            (request-messages))))

(handlers/register-handler-fx
 :inbox/request-messages
 (fn [cofx [_ args]]
   (request-messages args cofx)))

(handlers/register-handler-fx
 :inbox/connection-check
 (fn [{:keys [db] :as cofx} [_ _]]
   (when (= :connecting (:mailserver-status db))
     (update-mailserver-status :error cofx))))

(handlers/register-handler-fx
 :inbox/remove-fetching-notification
 (fn [{:keys [db] :as cofx} [_ _]]
   {:db (dissoc db :inbox/fetching?)}))

(handlers/register-handler-fx
 :inbox/reconnect
 (fn [cofx [_ args]]
   (connect-to-mailserver cofx)))
