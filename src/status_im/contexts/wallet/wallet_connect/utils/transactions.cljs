(ns status-im.contexts.wallet.wallet-connect.utils.transactions
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [promesa.core :as promesa]
            [schema.core :as schema]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.rpc :as rpc]
            [utils.hex :as hex]
            [utils.money :as money]
            [utils.transforms :as transforms]))

(def ^:private ?string-or-number
  [:or number? string?])

(def ?transaction
  [:map
   [:to :string]
   [:from :string]
   [:value ?string-or-number]
   [:gas {:optional true} ?string-or-number]
   [:gasPrice {:optional true} ?string-or-number]
   [:gasLimit {:optional true} ?string-or-number]
   [:nonce {:optional true} ?string-or-number]
   [:maxFeePerGas {:optional true} ?string-or-number]
   [:maxPriorityFeePerGas {:optional true} ?string-or-number]
   [:input {:optional true} [:maybe :string]]
   [:data {:optional true} [:maybe :string]]])

(defn transaction-request?
  [event]
  (->> (data-store/get-request-method event)
       (contains? #{constants/wallet-connect-eth-send-transaction-method
                    constants/wallet-connect-eth-sign-transaction-method})))

;; NOTE: Currently we don't allow the user to configure the tx priority as we don't
;; show the estimated time, but when we implement it, we should allow to change it
(def ^:constant default-tx-priority :medium)

(defn strip-hex-prefix
  "Strips the extra 0 in hex value if present"
  [hex-value]
  (let [formatted-hex (string/replace hex-value #"^0x0*" "0x")]
    (if (= formatted-hex "0x")
      "0x0"
      formatted-hex)))

(defn format-tx-hex-values
  "Apply f on transaction keys that are hex numbers"
  [tx f]
  (let [tx-keys [:gasLimit :gas :gasPrice :nonce :value :maxFeePerGas :maxPriorityFeePerGas]]
    (reduce (fn [acc tx-key]
              (if (and (contains? tx tx-key)
                       (not (nil? (get tx tx-key))))
                (update acc tx-key f)
                acc))
            tx
            tx-keys)))

(schema/=> format-tx-hex-values
  [:=>
   [:catn
    [:tx ?transaction]
    [:f fn?]]
   ?transaction])

(defn prepare-transaction-for-rpc
  "Formats the transaction and transforms it into a stringified JS object, ready to be passed to an RPC call."
  [tx]
  (-> tx
      ;; NOTE: removing `:nonce` to compute it when building the transaction on status-go
      (dissoc :nonce)
      (format-tx-hex-values strip-hex-prefix)
      bean/->js
      (transforms/js-stringify 0)))

(schema/=> prepare-transaction-for-rpc
  [:=>
   [:cat ?transaction]
   :string])

(defn beautify-transaction
  [tx]
  (-> tx
      (format-tx-hex-values hex/hex-to-number)
      clj->js
      (js/JSON.stringify nil 2)))

(schema/=> beautify-transaction
  [:=>
   [:cat ?transaction]
   :string])

(defn gwei->hex
  [gwei]
  (->> gwei
       money/gwei->wei
       native-module/number-to-hex
       (str "0x")))

(schema/=> gwei->hex
  [:=>
   [:cat ?string-or-number]
   :string])

(defn- get-max-fee-per-gas-key
  "Mapping transaction priority (which determines how quickly a tx is processed)
  to the `suggested-routes` key that should be used for `:maxPriorityFeePerGas` "
  [tx-priority]
  (get {:high   :maxFeePerGasHigh
        :medium :maxFeePerGasMedium
        :low    :maxFeePerGasLow}
       tx-priority))

(def ?tx-priority [:enum :high :medium :low])
(def ?max-fee-priority [:enum :maxFeePerGasHigh :maxFeePerGasMedium :maxFeePerGasLow])

(schema/=> get-max-fee-per-gas-key
  [:=>
   [:cat ?tx-priority]
   ?max-fee-priority])

(defn dynamic-fee-tx?
  "Checks if a transaction has dynamic fees (EIP1559)"
  [tx]
  (every? tx [:maxFeePerGas :maxPriorityFeePerGas]))

(schema/=> dynamic-fee-tx?
  [:=>
   [:cat ?transaction]
   :boolean])

(defn- tx->eip1559-tx
  "Adds `:maxFeePerGas` and `:maxPriorityFeePerGas` for dynamic fee support (EIP1559) and
  removes `:gasPrice`, if the chain supports EIP1559 and the transaction doesn't already
  have dynamic fees."
  [tx tx-priority suggested-fees]
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

(def ?suggested-fees
  [:map
   [:eip1559Enabled boolean?]
   [:maxFeePerGasLow number?]
   [:maxFeePerGasMedium number?]
   [:maxFeePerGasHigh number?]
   [:maxPriorityFeePerGas number?]
   [:gasPrice {:optional true} number?]
   [:baseFee {:optional true} number?]
   [:l1GasFee {:optional true} number?]])

(schema/=> tx->eip1559-tx
  [:=>
   [:catn
    [:tx ?transaction]
    [:tx-priority ?tx-priority]
    [:suggested-fees ?suggested-fees]]
   ?transaction])

(defn rename-gas-limit
  [tx]
  (if (:gasLimit tx)
    (-> tx
        ;; NOTE: `gasLimit` is ignored on status-go when building a transaction
        ;; (`wallet_buildTransaction`), so we're setting it as the `gas` property
        (assoc :gas (:gasLimit tx))
        (dissoc :gasLimit))
    tx))

(defn prepare-transaction-fees
  "Makes sure the transaction has the correct gas and fees properties"
  [tx tx-priority suggested-fees]
  (-> tx
      rename-gas-limit
      (tx->eip1559-tx tx-priority suggested-fees)))

(schema/=> prepare-transaction-fees
  [:=>
   [:catn
    [:tx ?transaction]
    [:tx-priority ?tx-priority]
    [:suggested-fees ?suggested-fees]]
   ?transaction])

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

(schema/=> prepare-transaction
  [:=>
   [:catn
    [:tx ?transaction]
    [:chain-id :int]
    [:tx-priority ?tx-priority]]
   [:map {:closed true}
    [:tx-args :string]
    [:tx-hash :string]
    [:suggested-fees ?suggested-fees]]])

(defn sign-transaction
  [password address tx-hash tx-args chain-id]
  (promesa/let
    [signature (rpc/wallet-sign-message tx-hash address password)
     raw-tx    (rpc/wallet-build-raw-transaction chain-id tx-args signature)]
    raw-tx))

(schema/=> sign-transaction
  [:=>
   [:catn
    [:password :string]
    [:address :string]
    [:tx-hash :string]
    [:tx-args ?transaction]
    [:chain-id :int]]
   :string])

(defn send-transaction
  [password address tx-hash tx-args chain-id]
  (promesa/let
    [signature (rpc/wallet-sign-message tx-hash address password)
     tx        (rpc/wallet-send-transaction-with-signature chain-id
                                                           tx-args
                                                           signature)]
    tx))

(schema/=> sign-transaction
  [:=>
   [:catn
    [:password :string]
    [:address :string]
    [:tx-hash :string]
    [:tx-args ?transaction]
    [:chain-id :int]]
   :string])
