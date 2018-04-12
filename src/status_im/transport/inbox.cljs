(ns ^{:doc "Offline inboxing events and API"}
    status-im.transport.inbox
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.utils.handlers :as handlers]
            [status-im.transport.utils :as web3.utils]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]
            [status-im.utils.ethereum.core :as ethereum]))

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

(defn get-current-wnode-address [{:accounts/keys [current-account-id accounts] :as db}]
  (let [network  (ethereum/network->chain-keyword (get db :network))
        wnode-id (get-in accounts [current-account-id :settings :wnode network])]
    (get-in db [:inbox/wnodes network wnode-id :address])))

(defn initialize-offline-inbox
  "Initialises offline inbox by producing `::add-peer` effect if inboxing enabled in config,
  then the event chan is:
  add-peer -> fetch-peers -> check-peer-added -> mark-trusted-peer -> get-sym-key -> request-messages"
  [{:keys [db]}]
  (when config/offline-inbox-enabled?
    (let [wnode (get-current-wnode-address db)]
      (log/info "offline inbox: initialize")
      {::add-peer {:wnode wnode}})))

(defn add-peer [enode success-fn error-fn]
  (status/add-peer enode (response-handler error-fn success-fn)))

(defn fetch-peers
  ;; https://github.com/ethereum/go-ethereum/wiki/Management-APIs#admin_peers
  ;; retrieves all the information known about the connected remote nodes
  ;; TODO(dmitryn): use web3 instead of rpc call
  [success-fn error-fn]
  (let [args    {:jsonrpc "2.0"
                 :id      2
                 :method  "admin_peers"
                 :params  []}
        payload (.stringify js/JSON (clj->js args))]
    (status/call-web3 payload (response-handler error-fn success-fn))))

(defn registered-peer? [peers enode]
  (let [peer-ids (into #{} (map :id) peers)
        enode-id (web3.utils/extract-enode-id enode)]
    (contains? peer-ids enode-id)))

(defn mark-trusted-peer [web3 enode peers success-fn error-fn]
  (.markTrustedPeer (web3.utils/shh web3)
                    enode
                    (fn [err resp]
                      (if-not err
                        (success-fn resp)
                        (error-fn err)))))

(defn request-messages [web3 wnode topics to from sym-key-id success-fn error-fn]
  (log/info "offline inbox: sym-key-id" sym-key-id)
  (let [opts (merge {:mailServerPeer wnode
                     :symKeyID       sym-key-id}
                    (when from {:from from})
                    (when to {:to to}))]
    (log/info "offline inbox: request-messages request for topics " topics)
    (doseq [topic topics]
      (let [opts (assoc opts :topic topic)]
        (log/info "offline inbox: request-messages args" (pr-str opts))
        (.requestMessages (web3.utils/shh web3)
                          (clj->js opts)
                          (fn [err resp]
                            (if-not err
                              (success-fn resp)
                              (error-fn err topic))))))))

(re-frame/reg-fx
  ::add-peer
  (fn [{:keys [wnode]}]
    (add-peer wnode
              #(re-frame/dispatch [::fetch-peers %])
              #(log/error "offline inbox: add-peer error" %))))

(re-frame/reg-fx
  ::fetch-peers
  (fn [retries]
    (fetch-peers #(re-frame/dispatch [::check-peer-added % retries])
                 #(log/error "offline inbox: fetch-peers error" %))))

(re-frame/reg-fx
  ::mark-trusted-peer
  (fn [{:keys [wnode web3 peers]}]
    (mark-trusted-peer web3
                       wnode
                       peers
                       #(re-frame/dispatch [::get-sym-key %])
                       #(log/error "offline inbox: mark-trusted-peer error" % wnode))))

(re-frame/reg-fx
  ::request-messages
  (fn [{:keys [wnode topics to from sym-key-id web3]}]
    (request-messages web3
                      wnode
                      topics
                      to
                      from
                      sym-key-id
                      #(log/info "offline inbox: request-messages response" %)
                      #(log/error "offline inbox: request-messages error" %1 %2 to from))))

;;;; Handlers

(handlers/register-handler-fx
  ::fetch-peers
  ;; This event fetches the list of peers
  ;; We want it to check if the node has been added
  (fn [_ [_ retries]]
    {::fetch-peers (or retries 0)}))

(handlers/register-handler-fx
  ::check-peer-added
  ;; We check if the wnode is part of the peers list
  ;; if not we dispatch a new fetch-peer event for later
  (fn [{:keys [db]} [_ peers retries]]
    (let [web3     (:web3 db)
          wnode    (get-current-wnode-address db)]
      (log/info "offline inbox: fetch-peers response" peers)
      (if (registered-peer? peers wnode)
        {::mark-trusted-peer {:web3  web3
                              :wnode wnode
                              :peers peers}}
        (do
          (log/info "Peer" wnode "is not registered. Retrying fetch peers.")
          (let [delay (if (< retries 3) 300 5000)]
            (if (> retries 10)
              (log/error "Could not connect to mailserver")
              {:dispatch-later [{:ms delay :dispatch [::fetch-peers (inc retries)]}]})))))))

(handlers/register-handler-fx
  ::get-sym-key
  ;; TODO(yenda): using core async flow this event can be done in parallel
  ;; with add-peer
  (fn [{:keys [db]} [_ response]]
    (let [web3     (:web3 db)
          wnode    (get-current-wnode-address db)
          password (:inbox/password db)]
      (log/info "offline inbox: mark-trusted-peer response" wnode response)
      {:shh/generate-sym-key-from-password {:password   password
                                            :web3       web3
                                            :on-success (fn [_ sym-key-id]
                                                          (re-frame/dispatch [::request-messages sym-key-id]))
                                            :on-error   #(log/error "offline inbox: get-sym-key error" %)}})))

(handlers/register-handler-fx
  ::request-messages
  ;; TODO(yenda): we want to request-message once per topic and for specific timespan so
  ;; we want a plural version of this function that does the right thing
  (fn [{:keys [db]} [_ sym-key-id]]
    (log/info "offline inbox: get-sym-key response") sym-key-id
    (let [web3     (:web3 db)
          wnode    (get-current-wnode-address db)
          topics   (map #(:topic %) (vals (:transport/chats db)))
          to       nil
          from     nil]
      {::request-messages {:wnode      wnode
                           :topics     topics
                           :to         to
                           :from       from
                           :sym-key-id sym-key-id
                           :web3       web3}})))
