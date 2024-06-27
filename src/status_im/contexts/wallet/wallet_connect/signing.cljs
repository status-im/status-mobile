(ns status-im.contexts.wallet.wallet-connect.signing
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [oops.core :as oops]
            [promesa.core :as promesa]
            [status-im.common.json-rpc.events :as rpc-events]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
            [utils.security.core :as security]
            [utils.transforms :as transforms]))

(defn- call-rpc
  [method & args]
  (promesa/create
   (fn [p-resolve p-reject]
     (rpc-events/call {:method      method
                       :params      args
                       :on-success  p-resolve
                       :on-error    p-reject
                       :js-response true}))))

(defn- strip-hex-prefix
  [hex-value]
  (let [formatted-hex (string/replace hex-value #"^0x0*" "0x")]
    (if (= formatted-hex "0x")
      "0x0"
      formatted-hex)))

(defn- prepare-rpc-data
  [data]
  (-> data
      bean/->js
      (transforms/js-stringify 0)))

(defn- format-tx-obj
  [tx]
  (let [tx-keys [:gasLimit :gasPrice :nonce :value]]
    (reduce (fn [acc tx-key]
              (if (and (contains? tx tx-key)
                       (not (nil? (get tx tx-key))))
                (update acc tx-key strip-hex-prefix)
                acc))
            tx
            tx-keys)))

(defn- wallet-build-transaction
  [chain-id tx]
  (let [formatted-tx (format-tx-obj tx)]
    (-> (call-rpc "wallet_buildTransaction" chain-id (prepare-rpc-data formatted-tx))
        (promesa/then #(hash-map :message-to-sign (oops/oget % "messageToSign")
                                 :tx-args         (oops/oget % "txArgs"))))))

(defn- wallet-build-raw-transaction
  [chain-id tx-args signature]
  (-> (call-rpc "wallet_buildRawTransaction"
                chain-id
                (transforms/js-stringify tx-args 0)
                (subs signature 2))
      (promesa/then #(oops/oget % "rawTx"))))

(defn- wallet-send-transaction-with-signature
  [chain-id tx-args signature]
  (call-rpc "wallet_sendTransactionWithSignature"
            chain-id
            constants/transaction-pending-type-wallet-connect-transfer
            (transforms/js-stringify tx-args 0)
            (subs signature 2)))

(defn sign-message
  [password address data]
  (-> {:data     data
       :account  address
       :password (security/safe-unmask-data password)}
      bean/->js
      transforms/clj->json
      native-module/sign-message
      (promesa/then wallet-connect-core/extract-native-call-signature)))

(defn sign-transaction
  [password address tx chain-id]
  (-> (promesa/let
        [{:keys [message-to-sign tx-args]} (wallet-build-transaction chain-id tx)
         signature                         (sign-message password address message-to-sign)
         raw-tx                            (wallet-build-raw-transaction chain-id tx-args signature)]
        raw-tx)))

(defn send-transaction
  [password address tx chain-id]
  (-> (promesa/let
        ;;FIXME dissoc
        [{:keys [message-to-sign tx-args]} (wallet-build-transaction chain-id
                                                                     (dissoc tx :nonce tx))
         signature                         (sign-message password address message-to-sign)
         tx                                (wallet-send-transaction-with-signature chain-id
                                                                                   tx-args
                                                                                   signature)]
        tx)))
