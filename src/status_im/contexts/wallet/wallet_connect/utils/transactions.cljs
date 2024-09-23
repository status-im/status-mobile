(ns status-im.contexts.wallet.wallet-connect.utils.transactions
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [promesa.core :as promesa]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.rpc :as rpc]
            [utils.money :as money]
            [utils.transforms :as transforms]))

(defn transaction-request?
  [event]
  (->> (data-store/get-request-method event)
       (contains? #{constants/wallet-connect-eth-send-transaction-method
                    constants/wallet-connect-eth-sign-transaction-method})))

;; NOTE: Currently we don't allow the user to configure the tx priority as we don't
;; show the estimated time, but when we implement it, we should allow to change it
(def ^:constant default-tx-priority :medium)

(defn- strip-hex-prefix
  "Strips the extra 0 in hex value if present"
  [hex-value]
  (let [formatted-hex (string/replace hex-value #"^0x0*" "0x")]
    (if (= formatted-hex "0x")
      "0x0"
      formatted-hex)))

(defn- format-tx-hex-values
  "Due to how status-go expects hex values, we should remove the extra 0s in transaction hex values e.g. 0x0f -> 0xf"
  [tx f]
  (let [tx-keys [:gasLimit :gas :gasPrice :nonce :value :maxFeePerGas :maxPriorityFeePerGas]]
    (reduce (fn [acc tx-key]
              (if (and (contains? tx tx-key)
                       (not (nil? (get tx tx-key))))
                (update acc tx-key f)
                acc))
            tx
            tx-keys)))

(defn- prepare-transaction-for-rpc
  "Formats the transaction and transforms it into a stringified JS object, ready to be passed to an RPC call."
  [tx]
  (-> tx
      ;; NOTE: removing `:nonce` to compute it when building the transaction on status-go
      (dissoc :nonce)
      (format-tx-hex-values strip-hex-prefix)
      bean/->js
      (transforms/js-stringify 0)))

(defn beautify-transaction
  [tx]
  (-> tx
      clj->js
      (js/JSON.stringify nil 2)))

(defn transaction-hex-values->number
  [tx]
  (let [hex->number #(-> % (subs 2) native-module/hex-to-number)]
    (-> tx
        (format-tx-hex-values hex->number))))

(defn- gwei->hex
  [gwei]
  (->> gwei
       money/gwei->wei
       native-module/number-to-hex
       (str "0x")))

(defn- get-max-fee-per-gas-key
  "Mapping transaction priority (which determines how quickly a tx is processed)
  to the `suggested-routes` key that should be used for `:maxPriorityFeePerGas`.

  Returns `:high` | `:medium` | `:low`"
  [tx-priority]
  (get {:high   :maxFeePerGasHigh
        :medium :maxFeePerGasMedium
        :low    :maxFeePerGasLow}
       tx-priority))

(defn- dynamic-fee-tx?
  "Checks if a transaction has dynamic fees (EIP1559)"
  [tx]
  (every? tx [:maxFeePerGas :maxPriorityFeePerGas]))

(defn- tx->eip1559-tx
  "Adds `:maxFeePerGas` and `:maxPriorityFeePerGas` for dynamic fee support (EIP1559) and
  removes `:gasPrice`, if the chain supports EIP1559 and the transaction doesn't already
  have dynamic fees."
  [tx suggested-fees tx-priority]
  (if (and (:eip1559Enabled suggested-fees)
           (not (dynamic-fee-tx? tx)))
    (let [max-fee-per-gas-key      (get-max-fee-per-gas-key tx-priority)
          max-fee-per-gas          (-> suggested-fees max-fee-per-gas-key gwei->hex)
          max-priority-fee-per-gas (-> suggested-fees :maxPriorityFeePerGas gwei->hex)]
      (-> tx
          (assoc
           :maxFeePerGas         max-fee-per-gas
           :maxPriorityFeePerGas max-priority-fee-per-gas)
          ;; NOTE: `:gasPrice` is used only for legacy Tx, so we discard it in favor of dynamic fees
          (dissoc :gasPrice)))
    tx))

(defn- prepare-transaction-fees
  "Makes sure the transaction has the correct gas and fees properties"
  [tx tx-priority suggested-fees]
  (-> (assoc tx
             ;; NOTE: `gasLimit` is ignored on status-go when building a transaction
             ;; (`wallet_buildTransaction`), so we're setting it as the `gas` property
             :gas
             (or (:gasLimit tx)
                 (:gas tx)))
      (dissoc :gasLimit)
      (tx->eip1559-tx suggested-fees tx-priority)))

(defn prepare-transaction
  "Formats and builds the incoming transaction, adding the missing properties and returning the final
  transaction, along with the transaction hash and the suggested fees"
  [tx chain-id tx-priority]
  (promesa/let [suggested-fees                    (rpc/wallet-get-suggested-fees chain-id)
                {:keys [tx-args message-to-sign]} (->>
                                                    (prepare-transaction-fees tx
                                                                              tx-priority
                                                                              suggested-fees)
                                                    prepare-transaction-for-rpc
                                                    (rpc/wallet-build-transaction chain-id))
                estimated-time                    (rpc/wallet-get-transaction-estimated-time
                                                   chain-id
                                                   (:maxPriorityFeePerGas suggested-fees))]
    {:tx-args        tx-args
     :tx-hash        message-to-sign
     :suggested-fees suggested-fees
     :estimated-time estimated-time}))

(defn sign-transaction
  [password address tx-hash tx-args chain-id]
  (promesa/let
    [signature (rpc/wallet-sign-message tx-hash address password)
     raw-tx    (rpc/wallet-build-raw-transaction chain-id tx-args signature)]
    raw-tx))

(defn send-transaction
  [password address tx-hash tx-args chain-id]
  (promesa/let
    [signature (rpc/wallet-sign-message tx-hash address password)
     tx        (rpc/wallet-send-transaction-with-signature chain-id
                                                           tx-args
                                                           signature)]
    tx))

(defn transactions->display-array
  [data]
  (remove (fn [[k v]]
            (println k " and value is " v)
            (or (= v "0x")
                (= k :MultiTransactionID)
                (= k :Symbol)))
          data))
