(ns status-im.extensions.ethereum
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.models.wallet :as models.wallet]
            [status-im.utils.hex :as hex]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.abi-spec :as abi-spec]
            [status-im.utils.fx :as fx]
            [status-im.utils.ethereum.ens :as ens]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]
            [clojure.string :as string]
            [status-im.utils.types :as types]
            [status-im.native-module.core :as status]))

(handlers/register-handler-fx
 :extensions/wallet-ui-on-result
 (fn [cofx [_ on-result id result method]]
   (fx/merge cofx
             (when on-result
               {:dispatch (on-result {:error nil :result result})})
             (cond
               (= method constants/web3-send-transaction) (navigation/navigate-to-clean :wallet-transaction-sent nil)
               (= method constants/web3-personal-sign) (navigation/navigate-back)))))

(handlers/register-handler-fx
 :extensions/wallet-ui-on-error
 (fn [{db :db} [_ on-result message]]
   (when on-result {:dispatch (on-result {:error message :result nil})})))

(defn- wrap-with-resolution [db arguments address-keyword f]
  "funtction responsible to resolve ens taken from argument
 and call the specified function with resolved address"
  (let [address (get arguments address-keyword)
        first-address (if (vector? address)  ;; currently we only support one ens for address
                        (first address)
                        address)]
    (if (ens/is-valid-eth-name? first-address)
      (let [{:keys [web3 network]} db
            network-info (get-in db [:account/account :networks network])
            chain (ethereum/network->chain-keyword network-info)
            registry (get ens/ens-registries chain)]
        (ens/get-addr web3 registry first-address #(f db (assoc arguments address-keyword %))))
      (f db arguments))))

;; EXTENSION TRANSACTION -> SEND TRANSACTION
(defn  prepare-extension-transaction [params contacts on-result]
  (let [{:keys [to value data gas gasPrice nonce]} params
        contact (get contacts (hex/normalize-hex to))]
    (cond-> {:id               "extension-id"
             :to-name          (or (when (nil? to)
                                     (i18n/label :t/new-contract))
                                   contact)
             :symbol           :ETH
             :method           constants/web3-send-transaction
             :to               to
             :amount           (money/bignumber (or value 0))
             :gas              (cond
                                 gas
                                 (money/bignumber gas)
                                 (and value (empty? data))
                                 (money/bignumber 21000))
             :gas-price        (when gasPrice
                                 (money/bignumber gasPrice))
             :data             data
             :on-result        [:extensions/wallet-ui-on-result on-result]
             :on-error         [:extensions/wallet-ui-on-error on-result]}
      nonce
      (assoc :nonce nonce))))

(defn- execute-send-transaction [db {:keys [method params on-result] :as arguments}]
  (let [tx-object (assoc (select-keys arguments [:to :gas :gas-price :value :nonce])
                         :data (when (and method params) (abi-spec/encode method params)))
        transaction (prepare-extension-transaction tx-object (:contacts/contacts db) on-result)]
    (models.wallet/open-modal-wallet-for-transaction db transaction tx-object)))

(handlers/register-handler-fx
 :extensions/ethereum-send-transaction
 (fn [{db :db} [_ _ arguments]]
   (wrap-with-resolution db arguments :to execute-send-transaction)))

(defn- execute-ethcall [_ {:keys [to method params outputs on-result]}]
  (let [tx-object {:to to :data (when method (abi-spec/encode method params))}]
    {:browser/call-rpc [{"jsonrpc" "2.0"
                         "method"  "eth_call"
                         "params"  [tx-object "latest"]}
                        #(let [result-str (when %2
                                            (get (js->clj %2) "result"))
                               result     (cond
                                            (= "0x" result-str) nil
                                            (and outputs result-str)
                                            (abi-spec/decode (string/replace result-str #"0x" "")  outputs)
                                            :else result-str)]
                           (re-frame/dispatch (on-result (merge {:result result} (when %1 {:error %1})))))]}))

(handlers/register-handler-fx
 :extensions/ethereum-call
 (fn [{db :db} [_ _ {:keys [to] :as arguments}]]
   (wrap-with-resolution db arguments :to execute-ethcall)))

(defn- parse-log [{:keys [address transactionHash blockHash transactionIndex topics blockNumber logIndex removed data]}]
  {:address           address
   :transaction-hash  transactionHash
   :blockHash         blockHash
   :transaction-index (abi-spec/hex-to-number transactionIndex)
   :topics            topics ;; TODO parse topics
   :block-number      (abi-spec/hex-to-number blockNumber)
   :log-index         (abi-spec/hex-to-number logIndex)
   :removed           removed
   :data              data})

(defn- parse-receipt [m]
  (when m
    (let [{:keys [status transactionHash transactionIndex blockHash blockNumber from to cumulativeGasUsed gasUsed contractAddress logs logsBloom]} m]
      {:status              (abi-spec/hex-to-number status)
       :transaction-hash    transactionHash
       :transaction-index   (abi-spec/hex-to-number transactionIndex)
       :block-hash          blockHash
       :block-number        (abi-spec/hex-to-number blockNumber)
       :from                from
       :to                  to
       :cumulative-gas-used (abi-spec/hex-to-number cumulativeGasUsed)
       :gas-used            (abi-spec/hex-to-number gasUsed)
       :contract-address    contractAddress
       :logs                (map parse-log logs)
       :logs-bloom          logsBloom})))

(handlers/register-handler-fx
 :extensions/ethereum-transaction-receipt
 (fn [_ [_ _ {:keys [value on-result]}]]
   (let [args {:jsonrpc "2.0"
               :method constants/web3-transaction-receipt
               :params  [value]}
         payload (types/clj->json args)]
     (status/call-private-rpc payload #(let [{:keys [error result]} (types/json->clj %1)
                                             response (merge {:result (parse-receipt result)} (when error {:error error}))]
                                         (re-frame/dispatch (on-result response)))))))

;; eth_getLogs implementation

(defn- event-topic-enc [event params]
  (let [eventid (str event "(" (string/join "," params) ")")]
    (abi-spec/sha3 eventid)))

(defn- types-mapping [type]
  (cond
    (= "bool" type) :bool
    (string/starts-with? type "uint") :uint
    (string/starts-with? type "int") :int
    (string/starts-with? type "address") :address
    (string/starts-with? type "bytes") :bytes
    (string/starts-with? type "fixed") :bytes
    :else :bytes))

(defn- values-topic-enc [type values]
  (let [mapped-type (types-mapping type)]
    (mapv #(str "0x" (abi-spec/enc {:type mapped-type :value %})) values)))

(defn- parse-topic [t]
  (cond
    (or (nil? t) (string? t)) t ;; nil topic ;; immediate topic (extension encode topic by its own) ;; vector of immediate topics
    (vector? t) (mapv parse-topic t) ;; descend in vector elements
    (map? t) ;; simplified topic interface, we need to encode
    (let [{:keys [event params type values]} t]
      (cond
        (some? event) (event-topic-enc event params);; event params topic
        (some? type) (values-topic-enc type values) ;; indexed values topic
        :else nil)) ;; error
    :else nil))

(defn- ensure-hex-bn [block]
  (cond
    (nil? block) block
    (re-matches #"^[0-9]+$" block) (str "0x" (abi-spec/number-to-hex block))
    :else block))

(defn- execute-get-logs [_ {:keys [fromBlock toBlock address topics blockhash on-result]}]
  (let [parsed-topics (mapv parse-topic topics)
        args {:jsonrpc "2.0"
              :method constants/web3-get-logs
              :params  [{:fromBlock (ensure-hex-bn fromBlock)
                         :toBlock   (ensure-hex-bn toBlock)
                         :address   address
                         :topics    parsed-topics
                         :blockhash blockhash}]}
        payload (types/clj->json args)]
    (status/call-private-rpc payload #(let [{:keys [error result]} (types/json->clj %1)
                                            response (merge {:result result} (when error {:error error}))]
                                        (re-frame/dispatch (on-result response))))))

(handlers/register-handler-fx
 :extensions/ethereum-logs
 (fn [{db :db} [_ _ arguments]]
   (wrap-with-resolution db arguments :address execute-get-logs)))

(handlers/register-handler-fx
 :extensions/ethereum-resolve-ens
 (fn [{db :db} [_ _ {:keys [name on-result]}]]
   (if (ens/is-valid-eth-name? name)
     (let [{:keys [web3 network]} db
           network-info (get-in db [:account/account :networks network])
           chain (ethereum/network->chain-keyword network-info)
           registry (get ens/ens-registries chain)]
       (ens/get-addr web3 registry name #(re-frame/dispatch (on-result {:result %}))))
     (re-frame/dispatch (on-result {:error (str "'" name "' is not a valid name")})))))

;; EXTENSION SIGN -> SIGN MESSAGE
(handlers/register-handler-fx
 :extensions/ethereum-sign
 (fn [{db :db :as cofx} [_ _ {:keys [message data id on-result]}]]
   (if (= (nil? message) (nil? data))
     {:dispatch (on-result {:error "only one of :message and :data can be used"})}
     (fx/merge cofx
               {:db (assoc-in db [:wallet :send-transaction]
                              {:id                id
                               :from             (ethereum/normalized-address  (get-in db [:account/account :address]))
                               :data             (or data (str "0x" (abi-spec/from-utf8 message)))
                               :on-result        [:extensions/wallet-ui-on-result on-result]
                               :on-error         [:extensions/wallet-ui-on-error on-result]
                               :method           constants/web3-personal-sign})}
               (navigation/navigate-to-cofx :wallet-sign-message-modal nil)))))

;; poll logs implementation
(handlers/register-handler-fx
 :extensions/ethereum-logs-changes
 (fn [_ [_ _ {:keys [filterId on-result]}]]
   (let [args {:jsonrpc "2.0"
               :method constants/web3-get-filter-changes
               :params [filterId]}
         payload (types/clj->json args)]
     (status/call-private-rpc payload #(let [{:keys [error result]} (types/json->clj %1)
                                             response (merge {:result result} (when error {:error error}))]
                                         (mapv (fn [one-result]
                                                 (re-frame/dispatch (on-result one-result))) result))))))
(handlers/register-handler-fx
 :extensions/ethereum-cancel-filter
 (fn [_ [_ _ {:keys [filterId on-result]}]]
   (let [args {:jsonrpc "2.0"
               :method constants/web3-uninstall-filter
               :params  [filterId]}
         payload (types/clj->json args)]
     (status/call-private-rpc payload #(let [{:keys [error result]} (types/json->clj %1)
                                             response (merge {:result result} (when error {:error error}))]
                                         (re-frame/dispatch (on-result response)))))))

(handlers/register-handler-fx
 :extensions/ethereum-create-filter
 (fn [_ [_ _ {:keys [filter-type from to address topics block-hash on-result]}]]
   (let [parsed-topics (mapv parse-topic topics)
         args (case filter-type
                "filter" {:jsonrpc "2.0"
                          :method constants/web3-new-filter
                          :params  [{:fromBlock (ensure-hex-bn from)
                                     :toBlock   (ensure-hex-bn to)
                                     :address   address
                                     :topics    parsed-topics
                                     :blockhash block-hash}]}
                "block" {:jsonrpc "2.0"
                         :method constants/web3-new-block-filter}
                "pendingTransaction" {:jsonrpc "2.0"
                                      :method constants/web3-new-pending-transaction-filter})
         payload (types/clj->json args)]
     (status/call-private-rpc payload #(let [{:keys [error result]} (types/json->clj %1)
                                             response (merge {:result result} (when error {:error error}))]
                                         (re-frame/dispatch (on-result response)))))))
