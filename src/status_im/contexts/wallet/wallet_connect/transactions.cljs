(ns status-im.contexts.wallet.wallet-connect.transactions
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [oops.core :as oops]
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

(defn- strip-hex-prefix
  "Strips the extra 0 in hex value if present"
  [hex-value]
  (let [formatted-hex (string/replace hex-value #"^0x0*" "0x")]
    (if (= formatted-hex "0x")
      "0x0"
      formatted-hex)))

(defn- format-tx-hex-values
  "Due to how status-go expects hex values, we should remove the extra 0s in transaction hex values e.g. 0x0f -> 0xf"
  [tx]
  (let [tx-keys [:gasLimit :gas :gasPrice :nonce :value :maxFeePerGas :maxPriorityFeePerGas]]
    (reduce (fn [acc tx-key]
              (if (and (contains? tx tx-key)
                       (not (nil? (get tx tx-key))))
                (update acc tx-key strip-hex-prefix)
                acc))
            tx
            tx-keys)))

(defn- prepare-transaction-for-rpc
  "Formats the transaction and transforms it into a stringified JS object, ready to be passed to an RPC call."
  [tx]
  (-> tx
      format-tx-hex-values
      bean/->js
      (transforms/js-stringify 0)))

(defn wallet-sign-message-rpc
  [password address data]
  (-> (call-rpc "wallet_signMessage"
                data
                address
                password)
      ;; NOTE: removing `0x`, as status-go expects the signature without it.
      (promesa/then #(subs % 2))))

(defn- wallet-build-transaction-rpc
  [chain-id tx]
  (-> (call-rpc "wallet_buildTransaction" chain-id tx)
      (promesa/then #(hash-map :message-to-sign (oops/oget % "messageToSign")
                               :tx-args         (oops/oget % "txArgs")))))

(defn- wallet-build-raw-transaction-rpc
  [chain-id tx-args signature]
  (-> (call-rpc "wallet_buildRawTransaction"
                chain-id
                (transforms/js-stringify tx-args 0)
                signature)
      (promesa/then #(oops/oget % "rawTx"))))

(defn- wallet-send-transaction-with-signature-rpc
  [chain-id tx-args signature]
  (call-rpc "wallet_sendTransactionWithSignature"
            chain-id
            constants/transaction-pending-type-wallet-connect-transfer
            (transforms/js-stringify tx-args 0)
            signature))

(defn sign-transaction
  [password address tx chain-id]
  (promesa/let
    [formatted-tx                      (prepare-transaction-for-rpc tx)
     {:keys [message-to-sign tx-args]} (wallet-build-transaction-rpc chain-id formatted-tx)
     signature                         (wallet-sign-message-rpc password address message-to-sign)
     raw-tx                            (wallet-build-raw-transaction-rpc chain-id tx-args signature)]
    raw-tx))

(defn send-transaction
  [password address tx chain-id]
  (promesa/let
    [formatted-tx                      (prepare-transaction-for-rpc tx)
     {:keys [message-to-sign tx-args]} (wallet-build-transaction-rpc chain-id formatted-tx)
     signature                         (wallet-sign-message-rpc password address message-to-sign)
     tx                                (wallet-send-transaction-with-signature-rpc chain-id
                                                                                   tx-args
                                                                                   signature)]
    tx))
