(ns status-im.contexts.wallet.wallet-connect.transactions
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [promesa.core :as promesa]
            [status-im.contexts.wallet.wallet-connect.rpc :as rpc]
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

(defn sign-transaction
  [password address tx chain-id]
  (promesa/let
    [formatted-tx                      (prepare-transaction-for-rpc tx)
     {:keys [message-to-sign tx-args]} (rpc/wallet-build-transaction chain-id formatted-tx)
     signature                         (rpc/wallet-sign-message message-to-sign address password)
     raw-tx                            (rpc/wallet-build-raw-transaction chain-id tx-args signature)]
    raw-tx))

(defn send-transaction
  [password address tx chain-id]
  (promesa/let
    [formatted-tx                      (prepare-transaction-for-rpc tx)
     {:keys [message-to-sign tx-args]} (rpc/wallet-build-transaction chain-id formatted-tx)
     signature                         (rpc/wallet-sign-message message-to-sign address password)
     tx                                (rpc/wallet-send-transaction-with-signature chain-id
                                                                                   tx-args
                                                                                   signature)]
    tx))
