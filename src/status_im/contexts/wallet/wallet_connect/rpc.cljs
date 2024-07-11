(ns status-im.contexts.wallet.wallet-connect.rpc
  (:require [oops.core :as oops]
            [promesa.core :as promesa]
            [status-im.common.json-rpc.events :as rpc-events]
            [status-im.constants :as constants]
            [utils.transforms :as transforms]))

(defn- call-rpc
  "Helper to handle RPC calls to status-go as promises"
  [method & args]
  (promesa/create
   (fn [p-resolve p-reject]
     (rpc-events/call {:method      method
                       :params      (vec args)
                       :on-success  p-resolve
                       :on-error    p-reject
                       :js-response true}))))

(defn wallet-build-transaction
  [chain-id tx]
  (-> (call-rpc "wallet_buildTransaction" chain-id tx)
      (promesa/then #(hash-map :message-to-sign (oops/oget % "messageToSign")
                               :tx-args         (oops/oget % "txArgs")))))

(defn wallet-build-raw-transaction
  [chain-id tx-args signature]
  (-> (call-rpc "wallet_buildRawTransaction"
                chain-id
                (transforms/js-stringify tx-args 0)
                signature)
      (promesa/then #(oops/oget % "rawTx"))))

(defn wallet-send-transaction-with-signature
  [chain-id tx-args signature]
  (call-rpc "wallet_sendTransactionWithSignature"
            chain-id
            constants/transaction-pending-type-wallet-connect-transfer
            (transforms/js-stringify tx-args 0)
            signature))

(defn wallet-sign-message
  [message address password]
  (-> (call-rpc "wallet_signMessage"
                message
                address
                password)
      ;; NOTE: removing `0x`, as status-go expects the signature without it.
      (promesa/then #(subs % 2))))

(defn wallet-hash-message-eip-191
  [message]
  (call-rpc "wallet_hashMessageEIP191" message))

(defn wallet-safe-sign-typed-data
  [data address password chain-id legacy?]
  (call-rpc "wallet_safeSignTypedDataForDApps"
            data
            address
            password
            chain-id
            legacy?))

(defn wallet-get-suggested-fees
  [chain-id]
  (-> (call-rpc "wallet_getSuggestedFees" chain-id)
      (promesa/then transforms/js->clj)))
