(ns status-im.contexts.wallet.wallet-connect.transactions
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [promesa.core :as promesa]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.core :as core]
            [status-im.contexts.wallet.wallet-connect.rpc :as rpc]
            [utils.money :as money]
            [utils.transforms :as transforms]))

(defn transaction-request?
  [event]
  (->> (core/get-request-method event)
       (contains? #{constants/wallet-connect-eth-send-transaction-method
                    constants/wallet-connect-eth-sign-transaction-method})))

;; NOTE: Currently we don't allow the user to configure the tx priority as we don't
;; show the estimated time, but when we implement it, we should allow to change it
(def ^:constant default-tx-priority :medium)

;; NOTE: Currently the `wallet_buildTransaction` RPC doesn't estimate the `gas` for dynamic
;; transactions, if `gas` is not present in the original transaction. Temporarily setting the
;; default `gas` till the issue is fixed on status-go
(def ^:constant default-gas
  (->> 21000
       native-module/number-to-hex
       (str "0x")))

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

;; QUESTION: should we remove the nonce from the original tx so it's always set by status-go, so that
;; we maintain the order of tx?
;;
;; Metamask seems to do so:
;; https://docs.metamask.io/wallet/how-to/send-transactions#nonce
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

(defn- gwei->hex
  [gwei]
  (->> gwei
       money/gwei->wei
       native-module/number-to-hex
       (str "0x")))

(defn- get-max-fee-per-gas-key
  "Mapping transaction priority (which determines how quickly a tx is processed)
  to the \"suggested-routes\" key that should be used for `:maxPriorityFeePerGas`.

  `:high` | `:medium` | `:low`"
  [tx-priority]
  (get {:high   :maxFeePerGasHigh
        :medium :maxFeePerGasMedium
        :low    :maxFeePerGasLow}
       tx-priority))

(defn- tx->eip1559-tx
  "Adds `:maxFeePerGas` and `:maxPriorityFeePerGas` for dynamic fee support (EIP1559) and
  removes `:gasPrice`, if the chain supports EIP1559"
  [tx suggested-fees tx-priority]
  (if (:eip1559Enabled suggested-fees)
    (let [max-fee-per-gas-key      (get-max-fee-per-gas-key tx-priority)
          max-fee-per-gas          (-> suggested-fees max-fee-per-gas-key gwei->hex)
          max-priority-fee-per-gas (-> suggested-fees :maxPriorityFeePerGas gwei->hex)]
      (-> tx
          (assoc
           :maxFeePerGas         max-fee-per-gas
           :maxPriorityFeePerGas max-priority-fee-per-gas)
          ;; NOTE: `:gasPrice` is used only for legacy TX, so we discard it in favor of dynamic fees
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
                                                    (rpc/wallet-build-transaction chain-id))]
    {:tx-args        tx-args
     :tx-hash        message-to-sign
     :suggested-fees suggested-fees}))

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
