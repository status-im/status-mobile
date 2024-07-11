(ns status-im.contexts.wallet.wallet-connect.transactions
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [promesa.core :as promesa]
            [status-im.contexts.wallet.wallet-connect.rpc :as rpc]
            [utils.money :as money]
            [utils.transforms :as transforms]))

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
      (format-tx-hex-values strip-hex-prefix)
      bean/->js
      (transforms/js-stringify 0)))

(defn beautify-transaction
  [tx]
  (let [hex->number #(-> % (subs 2) native-module/hex-to-number)]
    (-> tx
        (format-tx-hex-values hex->number)
        clj->js
        (js/JSON.stringify nil 2))))

(defn- tx->eip1559-tx
  [tx suggested-fees]
  (let [format-fee               #(->> %
                                       money/gwei->wei
                                       native-module/number-to-hex
                                       (str "0x"))
        max-fee-per-gas          (-> suggested-fees :maxFeePerGasHigh format-fee)
        max-priority-fee-per-gas (-> suggested-fees :maxPriorityFeePerGas format-fee)]
    (assoc tx
           :maxFeePerGas         max-fee-per-gas
           :maxPriorityFeePerGas max-priority-fee-per-gas)))

(defn prepare-transaction
  [tx chain-id]
  (promesa/->>
    (rpc/wallet-get-suggested-fees chain-id)
    (tx->eip1559-tx tx)
    prepare-transaction-for-rpc
    (rpc/wallet-build-transaction chain-id)))

(comment
  (-> {:from                 "0xb18ec1808bd8b84f244c6e34cbedee9b0cd7e1fb"
       :to                   "0xb18ec1808bd8b84f244c6e34cbedee9b0cd7e1fb"
       :gas                  "0x5208"
       :gasPrice             "0x3bf9965a7"
       :value                "0x0"
       :nonce                "0x8"
       :maxFeePerGas         nil
       :maxPriorityFeePerGas nil
       :input                "0x"
       :data                 "0x"
       :MultiTransactionID   0
       :Symbol               ""}
      (prepare-transaction 1)
      (promesa/then #(println "prepared transaction" %))))

(defn sign-transaction
  [password address built-tx-data chain-id]
  (promesa/let
    [{:keys [message-to-sign tx-args]} built-tx-data
     signature                         (rpc/wallet-sign-message message-to-sign address password)
     raw-tx                            (rpc/wallet-build-raw-transaction chain-id tx-args signature)]
    raw-tx))

(defn send-transaction
  [password address built-tx-data chain-id]
  (println built-tx-data)
  (promesa/let
    [{:keys [message-to-sign tx-args]} built-tx-data
     signature                         (rpc/wallet-sign-message message-to-sign address password)
     tx                                (rpc/wallet-send-transaction-with-signature chain-id
                                                                                   tx-args
                                                                                   signature)]
    tx))
