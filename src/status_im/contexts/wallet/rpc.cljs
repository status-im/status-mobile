(ns status-im.contexts.wallet.rpc
  (:require [oops.core :as oops]
            [promesa.core :as promesa]
            [status-im.common.json-rpc.events :as rpc-events]
            [status-im.constants :as constants]
            [utils.hex]
            [utils.transforms :as transforms]))

(defn build-transaction
  [chain-id tx]
  (promesa/let [res (rpc-events/call-async "wallet_buildTransaction" true chain-id tx)]
    {:message-to-sign (oops/oget res :messageToSign)
     :tx-args         (oops/oget res :txArgs)}))

(defn build-raw-transaction
  [chain-id tx-args signature]
  (-> (rpc-events/call-async "wallet_buildRawTransaction"
                             true
                             chain-id
                             (transforms/js-stringify tx-args 0)
                             signature)
      (promesa/then #(oops/oget % "rawTx"))))

(defn hash-message-eip-191
  [message]
  (rpc-events/call-async "wallet_hashMessageEIP191" true message))

(defn safe-sign-typed-data
  [data address password chain-id legacy?]
  (rpc-events/call-async "wallet_safeSignTypedDataForDApps"
                         true
                         data
                         address
                         password
                         chain-id
                         legacy?))

(defn get-suggested-fees
  [chain-id]
  (-> (rpc-events/call-async "wallet_getSuggestedFees" true chain-id)
      (promesa/then transforms/js->clj)))

(defn send-transaction-with-signature
  [chain-id tx-args signature]
  (rpc-events/call-async "wallet_sendTransactionWithSignature"
                         true
                         chain-id
                         constants/transaction-pending-type-wallet-connect-transfer
                         (transforms/js-stringify tx-args 0)
                         signature))

(defn sign-message
  [message address password]
  (-> (rpc-events/call-async "wallet_signMessage"
                             true
                             message
                             address
                             password)
      (promesa/then utils.hex/normalize-hex)))
