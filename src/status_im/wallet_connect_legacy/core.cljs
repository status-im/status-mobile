(ns status-im.wallet-connect-legacy.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.fx :as fx]
            [status-im.signing.core :as signing]
            [status-im.utils.wallet-connect-legacy :as wallet-connect-legacy]
            [status-im.browser.core :as browser]
            [taoensso.timbre :as log]
            [status-im.utils.types :as types]))

(re-frame/reg-fx
 :wc-1-subscribe-to-events
 (fn [^js connector]
   (.on connector "session_request" (fn [_ payload]
                                      (re-frame/dispatch [:wallet-connect-legacy/proposal payload connector])))
   (.on connector "connect" (fn [_ payload]
                              (re-frame/dispatch [:wallet-connect-legacy/created payload])))
   (.on connector "call_request" (fn [_ payload]
                                   (re-frame/dispatch [:wallet-connect-legacy/request-received (types/js->clj payload) connector])))
   (.on connector "session_update" (fn [_ payload]
                                     (re-frame/dispatch [:wallet-connect-legacy/update-sessions (types/js->clj payload) connector])))))

(re-frame/reg-fx
 :wc-1-approve-session
 (fn [[^js connector accounts proposal-chain-id]]
   (.approveSession connector (clj->js {:accounts accounts :chainId proposal-chain-id}))))

(re-frame/reg-fx
 :wc-1-reject-session
 (fn [^js connector]
   (.rejectSession connector)))

(re-frame/reg-fx
 :wc-1-update-session
 (fn [[^js connector chain-id address]]
   (.updateSession connector (clj->js {:chainId chain-id :accounts [address]}))))

(re-frame/reg-fx
 :wc-1-kill-session
 (fn [^js connector]
   (.killSession connector)))

(re-frame/reg-fx
 :wc-1-approve-request
 (fn [[^js connector response]]
   (.approveRequest connector (clj->js response))))

(fx/defn proposal-handler
  {:events [:wallet-connect-legacy/proposal]}
  [{:keys [db] :as cofx} request-event connector]
  (let [proposal (types/js->clj request-event)
        params (first (:params proposal))
        metadata (merge (:peerMeta params) {:wc-version constants/wallet-connect-version-1})
        networks (get db :networks/networks)
        current-network-id (get db :networks/current-network)
        current-network (get networks current-network-id)
        chain-id (get-in current-network [:config :NetworkId])]
    {:db (assoc db :wallet-connect-legacy/proposal-connector connector :wallet-connect-legacy/proposal-chain-id chain-id :wallet-connect/proposal-metadata metadata)
     :show-wallet-connect-sheet nil}))

(fx/defn session-connected
  {:events [:wallet-connect-legacy/created]}
  [{:keys [db]} session]
  (let [connector (get db :wallet-connect-legacy/proposal-connector)
        session (merge (types/js->clj session) {:wc-version constants/wallet-connect-version-1
                                                :connector connector})
        params (first (:params session))
        metadata (:peerMeta params)
        account (first (:accounts params))
        sessions (get db :wallet-connect-legacy/sessions)
        updated-sessions (if sessions (conj sessions session) [session])]
    (log/debug "[wallet connect 1.0] session created - " session)
    {:show-wallet-connect-success-sheet nil
     :db (assoc db :wallet-connect/session-connected session :wallet-connect-legacy/sessions updated-sessions)}))

(fx/defn manage-app
  {:events [:wallet-connect-legacy/manage-app]}
  [{:keys [db]} session]
  {:db (assoc db :wallet-connect/session-managed session :wallet-connect/showing-app-management-sheet? true)
   :show-wallet-connect-app-management-sheet nil})

(fx/defn request-handler
  {:events [:wallet-connect-legacy/request]}
  [{:keys [db] :as cofx} request-event]
  (let [request (types/js->clj request-event)
        params (:request request)
        pending-requests (or (:wallet-connect-legacy/pending-requests db) [])
        new-pending-requests (conj pending-requests request)
        client (get db :wallet-connect-legacy/client)
        topic (:topic request)]
    {:db (assoc db :wallet-connect-legacy/pending-requests new-pending-requests)
     :dispatch [:wallet-connect-legacy/request-received request]}))

(fx/defn request-handler-test
  {:events [:wallet-connect-legacy/request-test]}
  [{:keys [db] :as cofx}]
  {:show-wallet-connect-sheet nil})

(fx/defn approve-proposal
  {:events [:wallet-connect-legacy/approve-proposal]}
  [{:keys [db]} account]
  (let [connector (get db :wallet-connect-legacy/proposal-connector)
        proposal-chain-id (get db :wallet-connect-legacy/proposal-chain-id)
        address (ethereum/normalized-hex (:address account))
        accounts [address]]
    {:hide-wallet-connect-sheet nil
     :wc-1-approve-session [connector accounts proposal-chain-id]}))

(fx/defn reject-proposal
  {:events [:wallet-connect-legacy/reject-proposal]}
  [{:keys [db]} account]
  (let [connector (get db :wallet-connect-legacy/proposal-connector)]
    {:hide-wallet-connect-sheet nil
     :wc-1-reject-session connector}))

(fx/defn change-session-account
  {:events [:wallet-connect-legacy/change-session-account]}
  [{:keys [db]} session account]
  (let [connector (:connector session)
        address (:address account)
        networks (get db :networks/networks)
        current-network-id (get db :networks/current-network)
        current-network (get networks current-network-id)
        chain-id (get-in current-network [:config :NetworkId])]
    {:hide-wallet-connect-app-management-sheet nil
     :wc-1-update-session [connector chain-id address]
     :db (assoc db :wallet-connect/showing-app-management-sheet? false)}))

(fx/defn disconnect-session
  {:events [:wallet-connect-legacy/disconnect]}
  [{:keys [db]} session]
  (let [sessions (get db :wallet-connect-legacy/sessions)
        connector (:connector session)]
    {:hide-wallet-connect-app-management-sheet nil
     :hide-wallet-connect-success-sheet nil
     :wc-1-kill-session connector
     :db (-> db
             (assoc :wallet-connect-legacy/sessions (filter #(not= (:connector %) connector) sessions))
             (dissoc :wallet-connect/session-managed))}))

(fx/defn pair-session
  {:events [:wallet-connect-legacy/pair]}
  [{:keys [db]} {:keys [data]}]
  (let [connector (wallet-connect-legacy/create-connector data)
        wallet-connect-enabled? (get db :wallet-connect/enabled?)]
    (merge
     {:dispatch [:navigate-back]}
     (when wallet-connect-enabled?
       {:db (assoc db :wallet-connect-legacy/scanned-uri data)
        :wc-1-subscribe-to-events connector}))))

(fx/defn update-sessions
  {:events [:wallet-connect-legacy/update-sessions]}
  [{:keys [db] :as cofx} payload connector]
  (let [sessions (get db :wallet-connect-legacy/sessions)
        accounts-new (:accounts (first (:params payload)))
        session (first (filter #(= (:connector %) connector) sessions))
        updated-session (assoc-in session [:params 0 :accounts] accounts-new)]
    {:db (-> db
             (assoc :wallet-connect-legacy/sessions (conj (filter #(not= (:connector %) connector) sessions) updated-session))
             (dissoc :wallet-connect/session-managed))}))

(fx/defn wallet-connect-legacy-complete-transaction
  {:events [:wallet-connect-legacy.dapp/transaction-on-result]}
  [{:keys [db]} message-id connector result]
  (let [response {:id message-id
                  :result result}]
    {:db (assoc db :wallet-connect-legacy/response response)
     :wc-1-approve-request [connector response]}))

(fx/defn wallet-connect-legacy-send-async
  [{:keys [db] :as cofx} {:keys [method params id] :as payload} message-id connector]
  (let [message? (browser/web3-sign-message? method)
        sessions (get db :wallet-connect-legacy/sessions)
        session (first (filter #(= (:connector %) connector) sessions))
        linked-address (get-in session [:params 0 :accounts 0])
        accounts (get-in cofx [:db :multiaccount/visible-accounts])
        typed? (and (not= constants/web3-personal-sign method) (not= constants/web3-eth-sign method))]
    (if (or message? (= constants/web3-send-transaction method))
      (let [[address data] (cond (and (= method constants/web3-keycard-sign-typed-data)
                                      (not (vector? params)))
                                 ;; We don't use signer argument for keycard sign-typed-data
                                 ["0x0" params]
                                 message? (browser/normalize-sign-message-params params typed?)
                                 :else [nil nil])]
        (when (or (not message?) (and address data))
          (signing/sign cofx (merge
                              (if message?
                                {:message {:address address
                                           :data data
                                           :v4 (= constants/web3-sign-typed-data-v4 method)
                                           :typed? typed?
                                           :pinless? (= method constants/web3-keycard-sign-typed-data)
                                           :from address}}
                                {:tx-obj  (-> params
                                              first
                                              (update :from #(or % linked-address))
                                              (dissoc :gasPrice))})
                              {:on-result [:wallet-connect-legacy.dapp/transaction-on-result message-id connector]
                               :on-error  [:wallet-connect-legacy.dapp/transaction-on-error message-id connector]}))))
      (when (#{"eth_accounts" "eth_coinbase"} method)
        (wallet-connect-legacy-complete-transaction cofx message-id connector (if (= method "eth_coinbase") linked-address [linked-address]))))))

(fx/defn wallet-connect-legacy-send-async-read-only
  [{:keys [db] :as cofx} payload id connector]
  (wallet-connect-legacy-send-async cofx payload id connector))

(fx/defn process-request
  {:events [:wallet-connect-legacy/request-received]}
  [{:keys [db] :as cofx} payload connector]
  (let [{:keys [id]} payload]
    (wallet-connect-legacy-send-async-read-only cofx payload id connector)))
