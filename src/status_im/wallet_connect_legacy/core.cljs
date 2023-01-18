(ns status-im.wallet-connect-legacy.core
  (:require [re-frame.core :as re-frame]
            [status-im.browser.core :as browser]
            [status-im2.setup.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.signing.core :as signing]
            [utils.re-frame :as rf]
            [status-im.utils.types :as types]
            [status-im.utils.wallet-connect-legacy :as wallet-connect-legacy]
            [taoensso.timbre :as log]))

(defn subscribe-to-session
  [^js connector]
  (.on connector
       "session_request"
       (fn [err payload]
         (if err
           (log/error "session request error" err)
           (re-frame/dispatch [:wallet-connect-legacy/proposal payload connector]))))
  (.on connector
       "disconnect"
       (fn [err]
         ;; We pull the peer-id from the object we set it on
         (let [peer-id (or (.-connectedPeerId connector)
                           (.-peerId connector))]
           (if err
             (log/error "wallet connect error" err)
             (re-frame/dispatch [:wallet-connect-legacy/disconnect-by-peer-id peer-id])))))
  (.on connector
       "connect"
       (fn [err payload]
         (if err
           (log/error "connect error" err)
           (let [peer-id (get-in (types/js->clj payload) [:params 0 :peerId])]
             ;; This is extremely ugly. But.
             ;; Above in `disconnect` we don't have access to `peerId`
             ;; since it's not passed in the parameters, and it's cleared
             ;; from the connector object (I think by the library itself)
             ;; so we can't tell which peer-id we want to disconnect, and
             ;; therefore we can't remove it from the dabase.
             ;; So we set it on the connector object, and pull it back in
             ;; the disconnect event. May Rich Hickey have mercy on me.
             (set! (.. connector -connectedPeerId) peer-id)
             (re-frame/dispatch [:wallet-connect-legacy/created payload])))))
  (.on
   connector
   "call_request"
   (fn [err payload]
     (log/info "CALL REQUEST" (.-connectedPeerId connector))
     (if err
       (log/error "call request error" err)
       (re-frame/dispatch [:wallet-connect-legacy/request-received (types/js->clj payload) connector]))))
  (.on connector
       "session_update"
       (fn [err payload]
         (if err
           (log/error "session update error" err)
           (re-frame/dispatch [:wallet-connect-legacy/update-sessions (types/js->clj payload)
                               connector])))))

(re-frame/reg-fx
 :wc-1-subscribe-to-events
 subscribe-to-session)

(re-frame/reg-fx
 :initialize-wc-sessions
 (fn [[chain-id sessions]]
   (let [clj-sessions
         (doall
          (map
           (fn [^js session]
             (let [connector (wallet-connect-legacy/create-connector-from-session session)]
               ;; Update session so we are sure it's on the same network
               (.updateSession connector
                               (clj->js {:chainId  chain-id
                                         :accounts (.-accounts session)}))
               ;; Set the peerId
               (set! (.. connector -connectedPeerId) (.-peerId session))
               (subscribe-to-session connector)
               {:wc-version constants/wallet-connect-version-1
                :params     [{:peerId   (.-peerId session)
                              :peerMeta (types/js->clj (.-peerMeta session))
                              :chainId  (.-chainId session)
                              :accounts (types/js->clj (.-accounts session))}]
                :connector  connector}))
           sessions))]
     (re-frame/dispatch [::subscribed-to-multiple-sessions clj-sessions]))))

(re-frame/reg-fx
 :wc-1-approve-session
 (fn [[^js connector accounts proposal-chain-id]]
   (.approveSession connector (clj->js {:accounts accounts :chainId proposal-chain-id}))))

(re-frame/reg-fx
 :wc-1-reject-session
 (fn [^js connector]
   (.rejectSession connector)))

(re-frame/reg-fx
 :wc-1-reject-request
 (fn [[^js connector request-id message]]
   (.rejectRequest connector (clj->js {:id request-id :error {:message message}}))))

(re-frame/reg-fx
 :wc-1-clean-up-sessions
 (fn [^js connectors]
   (doseq [^js connector connectors]
     (.off connector "session_request")
     (.off connector "disconnect")
     (.off connector "connect")
     (.off connector "call_request")
     (.off connector "session_update"))))

(re-frame/reg-fx
 :wc-1-update-session
 (fn [[^js connector chain-id address]]
   (.updateSession connector (clj->js {:chainId chain-id :accounts [address]}))))

(re-frame/reg-fx
 :wc-1-kill-session
 (fn [^js connector]
   (log/debug "Kill wc session")
   (.killSession connector)))

(re-frame/reg-fx
 :wc-1-kill-sessions
 (fn [^js connectors]
   (log/debug "Kill wc sessions")
   (doseq [connector connectors]
     (.killSession ^js connector))))

(re-frame/reg-fx
 :wc-1-approve-request
 (fn [[^js connector response]]
   (.approveRequest connector (clj->js response))))

(rf/defn proposal-handler
  {:events [:wallet-connect-legacy/proposal]}
  [{:keys [db] :as cofx} request-event connector]
  (let [proposal           (types/js->clj request-event)
        params             (first (:params proposal))
        metadata           (merge (:peerMeta params) {:wc-version constants/wallet-connect-version-1})
        networks           (get db :networks/networks)
        current-network-id (get db :networks/current-network)
        current-network    (get networks current-network-id)
        chain-id           (get-in current-network [:config :NetworkId])]
    {:db                        (assoc db
                                       :wallet-connect-legacy/proposal-connector connector
                                       :wallet-connect-legacy/proposal-chain-id  chain-id
                                       :wallet-connect/proposal-metadata         metadata)
     :show-wallet-connect-sheet nil}))

(rf/defn clean-up-sessions
  {:events [:wallet-connect-legacy/clean-up-sessions]}
  [{:keys [db]}]
  (let [connectors (map
                    :connector
                    (:wallet-connect-legacy/sessions db))]
    {:wc-1-clean-up-sessions connectors}))

(rf/defn session-connected
  {:events [:wallet-connect-legacy/created]}
  [{:keys [db]} session]
  (let [connector (get db :wallet-connect-legacy/proposal-connector)
        session   (assoc (types/js->clj session)
                         :wc-version constants/wallet-connect-version-1
                         :connector  connector)
        info      (.stringify js/JSON (.-session connector))
        peer-id   (get-in session [:params 0 :peerId])
        dapp-name (get-in session [:params 0 :peerMeta :name])
        dapp-url  (get-in session [:params 0 :peerMeta :url])]
    {:show-wallet-connect-success-sheet nil
     :db                                (-> db
                                            (assoc :wallet-connect/session-connected session)
                                            (update :wallet-connect-legacy/sessions
                                                    conj
                                                    session))
     :json-rpc/call                     [{:method     "wakuext_addWalletConnectSession"
                                          :params     [{:id       peer-id
                                                        :info     info
                                                        :dappName dapp-name
                                                        :dappUrl  dapp-url}]
                                          :on-success
                                          #(log/info
                                            "wakuext_addWalletConnectSession success call back , data =>"
                                            %)
                                          :on-error
                                          #(log/error
                                            "wakuext_addWalletConnectSession error call back , data =>"
                                            %)}]}))

(rf/defn manage-app
  {:events [:wallet-connect-legacy/manage-app]}
  [{:keys [db]} session]
  {:db                                       (assoc db
                                                    :wallet-connect/session-managed               session
                                                    :wallet-connect/showing-app-management-sheet? true)
   :show-wallet-connect-app-management-sheet nil})

(rf/defn request-handler
  {:events [:wallet-connect-legacy/request]}
  [{:keys [db] :as cofx} request-event]
  (let [request              (types/js->clj request-event)
        params               (:request request)
        pending-requests     (or (:wallet-connect-legacy/pending-requests db) [])
        new-pending-requests (conj pending-requests request)
        client               (get db :wallet-connect-legacy/client)
        topic                (:topic request)]
    {:db       (assoc db :wallet-connect-legacy/pending-requests new-pending-requests)
     :dispatch [:wallet-connect-legacy/request-received request]}))

(rf/defn request-handler-test
  {:events [:wallet-connect-legacy/request-test]}
  [{:keys [db] :as cofx}]
  {:show-wallet-connect-sheet nil})

(rf/defn approve-proposal
  {:events [:wallet-connect-legacy/approve-proposal]}
  [{:keys [db]} account]
  (let [connector         (get db :wallet-connect-legacy/proposal-connector)
        proposal-chain-id (get db :wallet-connect-legacy/proposal-chain-id)
        address           (ethereum/normalized-hex (:address account))
        accounts          [address]]
    {:hide-wallet-connect-sheet nil
     :wc-1-approve-session      [connector accounts proposal-chain-id]}))

(rf/defn reject-proposal
  {:events [:wallet-connect-legacy/reject-proposal]}
  [{:keys [db]} account]
  (let [connector (get db :wallet-connect-legacy/proposal-connector)]
    {:hide-wallet-connect-sheet nil
     :wc-1-reject-session       connector}))

(rf/defn change-session-account
  {:events [:wallet-connect-legacy/change-session-account]}
  [{:keys [db]} session account]
  (let [connector          (:connector session)
        address            (:address account)
        networks           (get db :networks/networks)
        current-network-id (get db :networks/current-network)
        current-network    (get networks current-network-id)
        chain-id           (get-in current-network [:config :NetworkId])]
    {:hide-wallet-connect-app-management-sheet nil
     :wc-1-update-session                      [connector chain-id address]
     :db                                       (assoc db
                                                      :wallet-connect/showing-app-management-sheet?
                                                      false)}))

(rf/defn disconnect-by-peer-id
  {:events [:wallet-connect-legacy/disconnect-by-peer-id]}
  [{:keys [db]} peer-id]
  (let [sessions (get db :wallet-connect-legacy/sessions)]
    {:db            (-> db
                        (assoc :wallet-connect-legacy/sessions
                               (filter #(not= peer-id (get-in % [:params 0 :peerId])) sessions))
                        (dissoc :wallet-connect/session-managed)
                        (dissoc :wallet-connect/session-connected))
     :json-rpc/call [{:method     "wakuext_destroyWalletConnectSession"
                      :params     [peer-id]
                      :on-success #(log/debug
                                    "wakuext_destroyWalletConnectSession success call back , data ===>"
                                    %)
                      :on-error   #(log/debug
                                    "wakuext_destroyWalletConnectSession error call back , data ===>"
                                    %)}]}))

(rf/defn disconnect-session
  {:events [:wallet-connect-legacy/disconnect]}
  [{:keys [db]} session]
  (let [sessions  (get db :wallet-connect-legacy/sessions)
        connector (:connector session)
        peer-id   (get-in session [:params 0 :peerId])]
    {:hide-wallet-connect-app-management-sheet nil
     :hide-wallet-connect-success-sheet        nil
     :wc-1-kill-session                        connector
     :db                                       (-> db
                                                   (assoc :wallet-connect-legacy/sessions
                                                          (filter #(not= (:connector %) connector)
                                                                  sessions))
                                                   (dissoc :wallet-connect/session-managed)
                                                   (dissoc :wallet-connect/session-connected))
     :json-rpc/call
     [{:method     "wakuext_destroyWalletConnectSession"
       :params     [peer-id]
       :on-success #(log/debug "wakuext_destroyWalletConnectSession success call back , data ===>" %)
       :on-error   #(log/debug "wakuext_destroyWalletConnectSession error call back , data ===>" %)}]}))

(rf/defn pair-session
  {:events [:wallet-connect-legacy/pair]}
  [{:keys [db]} {:keys [data]}]
  (log/debug "uri received ===> ")
  (let [connector (wallet-connect-legacy/create-connector data)]
    {:db                       (assoc db :wallet-connect-legacy/scanned-uri data)
     :dispatch                 [:navigate-back]
     :wc-1-subscribe-to-events connector}))

(rf/defn update-sessions
  {:events [:wallet-connect-legacy/update-sessions]}
  [{:keys [db] :as cofx} payload connector]
  (let [sessions        (get db :wallet-connect-legacy/sessions)
        accounts-new    (:accounts (first (:params payload)))
        session         (first (filter #(= (:connector %) connector) sessions))
        updated-session (assoc-in session [:params 0 :accounts] accounts-new)]
    {:db (-> db
             (assoc :wallet-connect-legacy/sessions
                    (conj (filter #(not= (:connector %) connector) sessions) updated-session))
             (dissoc :wallet-connect/session-managed))}))

(rf/defn wallet-connect-legacy-complete-transaction
  {:events [:wallet-connect-legacy.dapp/transaction-on-result]}
  [{:keys [db]} message-id connector result]
  (let [response {:id     message-id
                  :result result}]
    {:db                   (assoc db :wallet-connect-legacy/response response)
     :wc-1-approve-request [connector response]}))

(rf/defn wallet-connect-legacy-transaction-error
  {:events [:wallet-connect-legacy.dapp/transaction-on-error]}
  [{:keys [db]} message-id connector message]
  {:wc-1-reject-request [connector message-id message]})

(rf/defn wallet-connect-legacy-send-async
  [{:keys [db] :as cofx} {:keys [method params id] :as payload} message-id connector]
  (let [message?       (browser/web3-sign-message? method)
        sessions       (get db :wallet-connect-legacy/sessions)
        session        (first (filter #(= (:connector %) connector) sessions))
        linked-address (get-in session [:params 0 :accounts 0])
        accounts       (get-in cofx [:db :multiaccount/visible-accounts])
        typed?         (and (not= constants/web3-personal-sign method)
                            (not= constants/web3-eth-sign method))]

    (if (or message? (= constants/web3-send-transaction method))
      (let [[address data] (cond (and (= method constants/web3-keycard-sign-typed-data)
                                      (not (vector? params)))
                                 ;; We don't use signer argument for keycard sign-typed-data
                                 ["0x0" params]
                                 message? (browser/normalize-sign-message-params params typed?)
                                 :else [nil nil])]
        (when (or (not message?) (and address data))
          (signing/sign
           cofx
           (merge
            (if message?
              {:message {:address  address
                         :data     data
                         :v4       (= constants/web3-sign-typed-data-v4 method)
                         :typed?   typed?
                         :pinless? (= method constants/web3-keycard-sign-typed-data)
                         :from     address}}
              {:tx-obj (-> params
                           first
                           (update :from #(or % linked-address))
                           (dissoc :gasPrice))})
            {:on-result [:wallet-connect-legacy.dapp/transaction-on-result message-id connector]
             :on-error  [:wallet-connect-legacy.dapp/transaction-on-error message-id connector]}))))
      (when (#{"eth_accounts" "eth_coinbase"} method)
        (wallet-connect-legacy-complete-transaction
         cofx
         message-id
         connector
         (if (= method "eth_coinbase") linked-address [linked-address]))))))

(rf/defn wallet-connect-legacy-send-async-read-only
  [{:keys [db] :as cofx} payload id connector]
  (wallet-connect-legacy-send-async cofx payload id connector))

(rf/defn process-request
  {:events [:wallet-connect-legacy/request-received]}
  [{:keys [db] :as cofx} payload connector]
  (let [{:keys [id]} payload]
    (wallet-connect-legacy-send-async-read-only cofx payload id connector)))

(rf/defn subscribed-to-multiple-sessions
  {:events [::subscribed-to-multiple-sessions]}
  [{:keys [db]} sessions]
  {:db (assoc db :wallet-connect-legacy/sessions sessions)})

(rf/defn sync-app-db-with-wc-sessions
  {:events [:sync-wallet-connect-app-sessions]}
  [{:keys [db]} session-data]
  (let [chain-id (get-in db [:networks/networks (:networks/current-network db) :config :NetworkId])
        sessions (->> session-data
                      (map :info)
                      (map js/JSON.parse)
                      (filter #(.-connected %)))] ; filter out non-connected-sessions
    (when chain-id
      {:initialize-wc-sessions [chain-id sessions]})))

(rf/defn get-connector-session-from-db
  {:events [:get-connector-session-from-db]}
  [_]
  {:json-rpc/call [{:method     "wakuext_getWalletConnectSession"
                    :on-success #(re-frame/dispatch [:sync-wallet-connect-app-sessions %])
                    :on-error   #(log/debug
                                  "wakuext_getWalletConnectSession error call back , data ===>"
                                  %)}]})
