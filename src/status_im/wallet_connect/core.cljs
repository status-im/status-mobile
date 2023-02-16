(ns status-im.wallet-connect.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.browser.core :as browser]
            [status-im2.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.signing.core :as signing]
            [status-im2.config :as config]
            [utils.re-frame :as rf]
            [status-im.utils.types :as types]
            [status-im.utils.wallet-connect :as wallet-connect]
            [taoensso.timbre :as log]))

(rf/defn proposal-handler
  {:events [:wallet-connect/proposal]}
  [{:keys [db] :as cofx} request-event]
  (let [proposal (types/js->clj request-event)
        proposer (:proposer proposal)
        metadata (:metadata proposer)]
    {:db                        (assoc db
                                       :wallet-connect/proposal          proposal
                                       :wallet-connect/proposal-metadata metadata)
     :show-wallet-connect-sheet nil}))

(rf/defn session-connected
  {:events [:wallet-connect/created]}
  [{:keys [db]} session]
  (let [session (merge (types/js->clj session) {:wc-version 2})
        client  (get db :wallet-connect/client)]
    (log/debug "[wallet connect] session created - " session)
    {:show-wallet-connect-success-sheet nil
     :db                                (assoc db
                                               :wallet-connect/session-connected session
                                               :wallet-connect/sessions          (types/js->clj
                                                                                  (.-values
                                                                                   (.-session
                                                                                    client))))}))

(rf/defn manage-app
  {:events [:wallet-connect/manage-app]}
  [{:keys [db]} session]
  (let [session (types/js->clj session)]
    {:db                                       (assoc db
                                                      :wallet-connect/session-managed session
                                                      :wallet-connect/showing-app-management-sheet? true)
     :show-wallet-connect-app-management-sheet nil}))

(rf/defn request-handler
  {:events [:wallet-connect/request]}
  [{:keys [db] :as cofx} request-event]
  (let [request              (types/js->clj request-event)
        params               (:request request)
        pending-requests     (or (:wallet-connect/pending-requests db) [])
        new-pending-requests (conj pending-requests request)
        client               (get db :wallet-connect/client)
        topic                (:topic request)]
    {:db       (assoc db :wallet-connect/pending-requests new-pending-requests)
     :dispatch [:wallet-connect/request-received request]}))

(rf/defn request-handler-test
  {:events [:wallet-connect/request-test]}
  [{:keys [db] :as cofx}]
  {:show-wallet-connect-sheet nil})

(defn subscribe-to-events
  [^js wallet-connect-client]
  (.on wallet-connect-client
       (wallet-connect/session-request-event)
       #(re-frame/dispatch [:wallet-connect/request %]))
  (.on wallet-connect-client
       (wallet-connect/session-created-event)
       #(re-frame/dispatch [:wallet-connect/created %]))
  (.on wallet-connect-client
       (wallet-connect/session-deleted-event)
       #(re-frame/dispatch [:wallet-connect/update-sessions]))
  (.on wallet-connect-client
       (wallet-connect/session-updated-event)
       #(re-frame/dispatch [:wallet-connect/update-sessions]))
  (.on wallet-connect-client
       (wallet-connect/session-proposal-event)
       #(re-frame/dispatch [:wallet-connect/proposal %])))

(re-frame/reg-fx
 :wc-2-init
 (fn []
   (wallet-connect/init
    #(re-frame/dispatch [:wallet-connect/client-init %])
    #(log/error "[wallet-connect]" %))))

(re-frame/reg-fx
 :wc-2-subscribe-to-events
 (fn [client]
   (subscribe-to-events client)))

(re-frame/reg-fx
 :wc-2-client-approve-proposal
 (fn [[client proposal response]]
   (-> ^js client
       (.approve (clj->js {:proposal proposal :response response}))
       (.then #(log/debug "[wallet-connect] session proposal approved"))
       (.catch #(log/error "[wallet-connect] session proposal approval error:" %)))))

(re-frame/reg-fx
 :wc-2-client-reject-proposal
 (fn [[client proposal]]
   (-> ^js client
       (.reject (clj->js {:proposal proposal}))
       (.then #(log/debug "[wallet-connect] session proposal rejected"))
       (.catch #(log/error "[wallet-connect] " %)))))

(re-frame/reg-fx
 :wc-2-client-disconnect
 (fn [[client topic]]
   (-> ^js client
       (.disconnect (clj->js {:topic topic}))
       (.then #(log/debug "[wallet-connect] session disconnected - topic " topic))
       (.catch #(log/error "[wallet-connect] " %)))))

(re-frame/reg-fx
 :wc-2-change-session
 (fn [[client topic accounts]]
   (-> ^js client
       (.update (clj->js {:topic topic
                          :state {:accounts accounts}}))
       (.then #(log/debug "[wallet-connect] session topic " topic
                          " changed to account "            (first accounts)))
       (.catch #(log/error "[wallet-connect] " %)))))

(re-frame/reg-fx
 :wc-2-pair
 (fn [[client uri]]
   (.pair client (clj->js {:uri uri}))))

(re-frame/reg-fx
 :wc-2-respond
 (fn [[client response]]
   (.respond client (clj->js response))))

(rf/defn approve-proposal
  {:events [:wallet-connect/approve-proposal]}
  [{:keys [db]} account]
  (let [client              (get db :wallet-connect/client)
        proposal            (get db :wallet-connect/proposal)
        topic               (:topic proposal)
        permissions         (:permissions proposal)
        blockchain          (:blockchain permissions)
        proposal-chain-ids  (map #(last (string/split % #":")) (:chains blockchain))
        available-chain-ids (map #(get-in % [:config :NetworkId]) (vals (get db :networks/networks)))
        supported-chain-ids (filter (fn [chain-id] #(boolean (some #{chain-id} available-chain-ids)))
                                    proposal-chain-ids)
        address             (:address account)
        accounts            (map #(str "eip155:" % ":" (ethereum/normalized-hex address))
                                 supported-chain-ids)
        ;; TODO: Check for unsupported
        metadata            (get db :wallet-connect/proposal-metadata)
        response            {:state    {:accounts accounts}
                             :metadata config/default-wallet-connect-metadata}]
    {:hide-wallet-connect-sheet    nil
     :wc-2-client-approve-proposal [client proposal response]}))

(rf/defn reject-proposal
  {:events [:wallet-connect/reject-proposal]}
  [{:keys [db]} account]
  (let [client   (get db :wallet-connect/client)
        proposal (get db :wallet-connect/proposal)]
    {:hide-wallet-connect-sheet   nil
     :wc-2-client-reject-proposal client}))

(rf/defn change-session-account
  {:events [:wallet-connect/change-session-account]}
  [{:keys [db]} topic account]
  (let [client              (get db :wallet-connect/client)
        sessions            (get db :wallet-connect/sessions)
        session             (first (filter #(= (:topic %) topic) sessions))
        permissions         (:permissions session)
        blockchain          (:blockchain permissions)
        proposal-chain-ids  (map #(last (string/split % #":")) (:chains blockchain))
        address             (:address account)
        available-chain-ids (map #(get-in % [:config :NetworkId]) (vals (get db :networks/networks)))
        supported-chain-ids (filter (fn [chain-id] #(boolean (some #{chain-id} available-chain-ids)))
                                    proposal-chain-ids)
        accounts            (map #(str "eip155:" % ":" (ethereum/normalized-hex address))
                                 supported-chain-ids)]
    {:db                                       (assoc db
                                                      :wallet-connect/showing-app-management-sheet?
                                                      false)
     :hide-wallet-connect-app-management-sheet nil
     :wc-2-change-session                      [client topic accounts]}))

(rf/defn disconnect-session
  {:events [:wallet-connect/disconnect]}
  [{:keys [db]} topic]
  (let [client (get db :wallet-connect/client)]
    {:hide-wallet-connect-app-management-sheet nil
     :hide-wallet-connect-success-sheet        nil
     :wc-2-client-disconnect                   [client topic]
     :db                                       (-> db
                                                   (assoc :wallet-connect/sessions
                                                          (types/js->clj (.-values (.-session client))))
                                                   (dissoc :wallet-connect/session-managed))}))

(rf/defn pair-session
  {:events [:wallet-connect/pair]}
  [{:keys [db]} {:keys [data]}]
  (let [client (get db :wallet-connect/client)]
    {:db        (assoc db :wallet-connect/scanned-uri data)
     :dispatch  [:navigate-back]
     :wc-2-pair [client data]}))

(rf/defn wallet-connect-client-initate
  {:events [:wallet-connect/client-init]}
  [{:keys [db] :as cofx} ^js client]
  {:db                       (assoc db
                                    :wallet-connect/client   client
                                    :wallet-connect/sessions (types/js->clj (.-values (.-session
                                                                                       client))))
   :wc-2-subscribe-to-events client})

(rf/defn update-sessions
  {:events [:wallet-connect/update-sessions]}
  [{:keys [db] :as cofx}]
  (let [client (get db :wallet-connect/client)]
    {:db (-> db
             (assoc :wallet-connect/sessions (types/js->clj (.-values (.-session client))))
             (dissoc :wallet-connect/session-managed))}))

(rf/defn wallet-connect-complete-transaction
  {:events [:wallet-connect.dapp/transaction-on-result]}
  [{:keys [db]} message-id topic result]
  (let [client   (get db :wallet-connect/client)
        response {:topic    topic
                  :response {:jsonrpc "2.0"
                             :id      message-id
                             :result  result}}]
    {:db           (assoc db :wallet-connect/response response)
     :wc-2-respond [client response]}))

(rf/defn wallet-connect-send-async
  [cofx {:keys [method params id] :as payload} message-id topic]
  (let [message?      (browser/web3-sign-message? method)
        dapps-address (get-in cofx [:db :multiaccount :dapps-address])
        accounts      (get-in cofx [:db :multiaccount/visible-accounts])
        typed?        (and (not= constants/web3-personal-sign method)
                           (not= constants/web3-eth-sign method))]
    (if (or message? (= constants/web3-send-transaction method))
      (let [[address data] (cond (and (= method constants/web3-keycard-sign-typed-data)
                                      (not (vector? params)))
                                 ;; We don't use signer argument for keycard sign-typed-data
                                 ["0x0" params]
                                 message? (browser/normalize-sign-message-params params typed?)
                                 :else [nil nil])]
        (when (or (not message?) (and address data))
          (signing/sign cofx
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
                                        (update :from #(or % dapps-address))
                                        (dissoc :gasPrice))})
                         {:on-result [:wallet-connect.dapp/transaction-on-result message-id topic]
                          :on-error  [:wallet-connect.dapp/transaction-on-error message-id topic]}))))
      (when (#{"eth_accounts" "eth_coinbase"} method)
        (wallet-connect-complete-transaction
         cofx
         message-id
         topic
         (if (= method "eth_coinbase") dapps-address [dapps-address]))))))

(rf/defn wallet-connect-send-async-read-only
  [{:keys [db] :as cofx} {:keys [method] :as payload} message-id topic]
  (wallet-connect-send-async cofx payload message-id topic))

(rf/defn process-request
  {:events [:wallet-connect/request-received]}
  [{:keys [db] :as cofx} session-request]
  (let [pending-requests        (get db :wallet-connect/pending-requests)
        {:keys [topic request]} session-request
        {:keys [id]}            request]
    (wallet-connect-send-async-read-only cofx request id topic)))
