(ns status-im.contexts.wallet.wallet-connect.utils.transactions
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [promesa.core :as promesa]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.rpc :as rpc]
            [utils.address :as address]
            [utils.hex :as hex]
            [utils.money :as money]
            [utils.transforms :as transforms]))

(defn transaction-request?
  [event]
  (->> (data-store/get-request-method event)
       (contains? #{constants/wallet-connect-eth-send-transaction-method})))

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

(defn transaction-estimations
  [chain-id]
  (promesa/let [suggested-fees (rpc/wallet-get-suggested-fees chain-id)
                estimated-time (->> suggested-fees
                                    :maxPriorityFeePerGas
                                    (rpc/wallet-get-transaction-estimated-time chain-id))]
    {:suggested-fees suggested-fees
     :estimated-time estimated-time}))

(defn prepare-transaction
  "Formats and builds the incoming transaction, adding the missing properties and returning the final
  transaction, along with the transaction hash and the suggested fees"
  [tx chain-id tx-priority]
  (promesa/let [{:keys [suggested-fees] :as estimations} (transaction-estimations chain-id)
                {:keys [tx-args message-to-sign]}        (->> suggested-fees
                                                              (prepare-transaction-fees tx
                                                                                        tx-priority)
                                                              prepare-transaction-for-rpc
                                                              (rpc/wallet-build-transaction chain-id))]
    {:tx-args        tx-args
     :tx-hash        message-to-sign
     :tx-estimations estimations}))

(defn send-transaction
  [password address tx-hash tx-args chain-id]
  (promesa/->> password
               (rpc/wallet-sign-message tx-hash address)
               (rpc/wallet-send-transaction-with-signature chain-id tx-args)))

(defn transactions->display-array
  [data]
  (remove (fn [[k v]]
            (or (= v "0x")
                (= k :MultiTransactionID)
                (= k :Symbol)))
          data))

(defrecord Transaction
  [version from to gas gasPrice value nonce maxFeePerGas
   maxPriorityFeePerGas input data multiTransactionID])

(defn create-transaction
  [tx]
  ;; TODO add malli check schema
  (map->Transaction tx))

(defn- non-empty-hex
  [hex-string]
  (let [normalized (hex/normalize-hex hex-string)]
    (when-not (string/blank? normalized)
      hex-string)))

(defn get-data
  [^Transaction tx]
  (let [{:keys [input data]} tx]
    (or (non-empty-hex input)
        (non-empty-hex data))))

(def function-selector-to-type
  {constants/method-id-transfer :transaction/erc-20-transfer
   constants/method-id-approve  :transaction/erc-20-approve})

(defn get-type
  [^Transaction tx]
  (let [tx-data       (get-data tx)
        eth-transfer? (nil? tx-data)
        contract-type (when-not eth-transfer?
                        (-> tx-data (subs 0 10) function-selector-to-type))]
    (cond
      eth-transfer?            :transaction/eth-transfer
      (keyword? contract-type) contract-type
      :else                    :transaction/unknown)))

(defn get-sender
  [^Transaction tx]
  (-> tx :from string/lower-case))

(defn- subs-bytes
  [hex-data start-bytes end-bytes]
  (when (-> hex-data count (/ 2) (>= end-bytes))
    (-> hex-data
        hex/normalize-hex
        (subs (* start-bytes 2)
              (* end-bytes 2)))))

(defn- decode-erc-20-transfer
  "Decode ERC20 transfer data, which has the following function signature:
  `approve(spender, amount)`"
  [tx-data]
  (when (>= (count tx-data) 138)
    (let [selector      (subs-bytes tx-data 0 4) ;; Function selector (first 4 bytes)
          recipient-hex (subs-bytes tx-data 16 36) ;; Recipient address (next 32 bytes)
          amount-hex    (subs-bytes tx-data 36 68)] ;; Amount (next 32 bytes)
      {:selector  selector
       :recipient (address/normalized-hex recipient-hex)
       :amount    (money/from-hex amount-hex)})))

(defn- decode-erc-20-approve
  "Decode ERC20 transfer data, which has the following function signature:
  `approve(spender, amount)`"
  [tx-data]
  (when (>= (count tx-data) 138)
    (let [selector    (subs-bytes tx-data 0 4) ;; Function selector (first 4 bytes)
          spender-hex (subs-bytes tx-data 16 36) ;; Spender address (next 32 bytes)
          amount-hex  (subs-bytes tx-data 36 68)] ;; Amount (next 32 bytes)
      {:selector selector
       :spender  (address/normalized-hex spender-hex)
       :amount   (money/from-hex amount-hex)})))

(defn get-recipient
  "Get the transaction recipient, depending on the transaction type"
  [^Transaction tx]
  (let [default (:to tx)]
    (condp = (get-type tx)
      :transaction/eth-transfer    default
      :transaction/erc-20-transfer (-> tx
                                       get-data
                                       decode-erc-20-transfer
                                       :recipient
                                       (or default))
      :transaction/erc-20-approve  (-> tx
                                       get-data
                                       decode-erc-20-approve
                                       :spender
                                       (or default))
      default)))

(defn get-amount
  [^Transaction tx]
  (let [default (-> tx :value money/from-hex)]
    (condp = (get-type tx)
      :transaction/eth-transfer    default
      :transaction/erc-20-transfer (-> tx get-data decode-erc-20-transfer :amount)
      :transaction/erc-20-approve  (-> tx get-data decode-erc-20-approve :amount)
      default)))

(->
  (decode-erc-20-transfer
   "0xa9059cbb00000000000000000000000097654628dd47c2d88fc9b3d0cc38a92e46a32cd400000000000000000000000000000000000000000000000a0af513f7628a8000"))

(def tt
  {:version 0
   :from "0xb18ec1808bd8b84f244c6e34cbedee9b0cd7e1fb"
   :to "0x744d70fdbe2ba4cf95131626614a1763df805b9e"
   :gas "0x21e76"
   :gasPrice "0x8b491f4cf"
   :value "0x0"
   :nonce "0x1b"
   :maxFeePerGas "0x8b491f4cf"
   :maxPriorityFeePerGas "0x3b9aca00"
   :input "0x"
   :data
   "0xa9059cbb00000000000000000000000097654628dd47c2d88fc9b3d0cc38a92e46a32cd400000000000000000000000000000000000000000000000a0af513f7628a8000"
   :multiTransactionID 0})

(get-amount tt)
(-> tt
    decode-erc-20-transfer)

(get-type tt)


(defn get-contract-address
  [^Transaction tx]
  (condp = (get-type tx)
    :transaction/erc-20-transfer (:to tx)
    :transaction/erc-20-approve  (:to tx)
    nil))
