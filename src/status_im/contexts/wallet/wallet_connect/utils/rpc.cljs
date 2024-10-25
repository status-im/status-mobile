(ns status-im.contexts.wallet.wallet-connect.utils.rpc
  (:require [oops.core :as oops]
            [promesa.core :as promesa]
            [status-im.common.json-rpc.events :as rpc-events]
            [status-im.constants :as constants]
            [utils.hex :as hex]
            [utils.transforms :as transforms]))

(defn wallet-build-transaction
  [chain-id tx]
  (promesa/let [res (rpc-events/call-async :wallet_buildTransaction true chain-id tx)]
    {:message-to-sign (oops/oget res :messageToSign)
     :tx-args         (oops/oget res :txArgs)}))

(defn wallet-build-raw-transaction
  [chain-id tx-args signature]
  (-> (rpc-events/call-async "wallet_buildRawTransaction"
                             true
                             chain-id
                             (transforms/js-stringify tx-args 0)
                             signature)
      (promesa/then #(oops/oget % "rawTx"))))

(defn wallet-send-transaction-with-signature
  [chain-id tx-args signature]
  (rpc-events/call-async "wallet_sendTransactionWithSignature"
                         true
                         chain-id
                         constants/transaction-pending-type-wallet-connect-transfer
                         (transforms/js-stringify tx-args 0)
                         signature))

(defn wallet-sign-message
  [message address password]
  (-> (rpc-events/call-async "wallet_signMessage"
                             true
                             message
                             address
                             password)
      (promesa/then hex/normalize-hex)))

(defn wallet-hash-message-eip-191
  [message]
  (rpc-events/call-async "wallet_hashMessageEIP191" true message))

(defn wallet-safe-sign-typed-data
  [data address password chain-id legacy?]
  (rpc-events/call-async "wallet_safeSignTypedDataForDApps"
                         true
                         data
                         address
                         password
                         chain-id
                         legacy?))

(defn wallet-get-suggested-fees
  [chain-id]
  (-> (rpc-events/call-async "wallet_getSuggestedFees" true chain-id)
      (promesa/then transforms/js->clj)))

(defn wallet-disconnect-persisted-session
  [topic]
  (rpc-events/call-async "wallet_disconnectWalletConnectSession" true topic))

(defn wallet-get-persisted-sessions
  ([]
   (let [now (-> (js/Date.) .getTime (quot 1000))]
     (wallet-get-persisted-sessions now)))
  ([expiry-timestamp]
   (rpc-events/call-async "wallet_getWalletConnectActiveSessions" false expiry-timestamp)))

(defn wallet-persist-session
  [session]
  (->> session
       transforms/clj->json
       (rpc-events/call-async "wallet_addWalletConnectSession" false)))

(defn wallet-get-transaction-estimated-time
  [chain-id max-fee-per-gas]
  (-> (rpc-events/call-async "wallet_getTransactionEstimatedTime" true chain-id max-fee-per-gas)
      (promesa/then transforms/js->clj)))
