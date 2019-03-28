(ns status-im.extensions.ethereum
  (:require [clojure.string :as string]
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
            [status-im.utils.types :as types]
            [status-im.native-module.core :as status]))

(handlers/register-handler-fx
 :extensions/wallet-ui-on-success
 (fn [cofx [_ on-success _ result _]]
   (fx/merge cofx
             (when on-success (on-success {:value result}))
             (navigation/navigate-back))))

(handlers/register-handler-fx
 :extensions/wallet-ui-on-failure
 (fn [_ [_ on-failure message]]
   (when on-failure (on-failure {:value message}))))

(defn- wrap-with-resolution
  "function responsible to resolve ens taken from argument
   and call the specified function with resolved address"
  [db arguments address-keyword f]
  (let [address (get arguments address-keyword)
        ;; currently we only support one ens for address
        first-address (if (vector? address)
                        (first address)
                        address)]
    (if (ens/is-valid-eth-name? first-address)
      (let [{:keys [network]} db
            network-info (get-in db [:account/account :networks network])
            chain (ethereum/network->chain-keyword network-info)
            registry (get ens/ens-registries chain)]
        (ens/get-addr registry first-address
                      #(f db (assoc arguments address-keyword %))))
      (f db arguments))))

(defn prepare-extension-transaction [params contacts on-success on-failure]
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
             :on-result        [:extensions/wallet-ui-on-success on-success]
             :on-error         [:extensions/wallet-ui-on-failure on-failure]}
      nonce
      (assoc :nonce nonce))))

(defn- execute-send-transaction [db {:keys [method params on-success on-failure] :as arguments}]
  (let [tx-object (assoc (select-keys arguments [:to :gas :gas-price :value :nonce])
                         :data (when (and method params) (abi-spec/encode method params)))
        transaction (prepare-extension-transaction tx-object (:contacts/contacts db) on-success on-failure)]
    (models.wallet/open-modal-wallet-for-transaction db transaction tx-object)))

(handlers/register-handler-fx
 :extensions/ethereum-send-transaction
 (fn [{db :db} [_ _ arguments]]
   (wrap-with-resolution db arguments :to execute-send-transaction)))

(defn- rpc-args [method params]
  {:jsonrpc "2.0"
   :method  method
   :params  params})

(defn- rpc-dispatch [error result f on-success on-failure]
  (when result
    (on-success {:value (f result)}))
  (when (and error on-failure)
    (on-failure {:value error})))

(defn- rpc-handler [o f on-success on-failure]
  (let [{:keys [error result]} (types/json->clj o)]
    (rpc-dispatch error result f on-success on-failure)))

(defn- rpc-call [method params f {:keys [on-success on-failure]}]
  (let [payload (types/clj->json (rpc-args method params))]
    (status/call-private-rpc payload #(rpc-handler % f on-success on-failure))))

(defn parse-call-result [o outputs]
  (let [result (get (js->clj o) "result")]
    (cond
      (= "0x" result) nil
      (and outputs result)
      (abi-spec/decode result outputs)
      :else result)))

(defn- execute-ethcall [_ {:keys [to method params outputs on-success on-failure]}]
  (let [tx-object {:to to :data (when method (abi-spec/encode method params))}]
    {:browser/call-rpc [(rpc-args "eth_call" [tx-object "latest"])
                        #(rpc-dispatch %1 %2 (fn [o] (parse-call-result o outputs)) on-success on-failure)]}))

(handlers/register-handler-fx
 :extensions/ethereum-call
 (fn [{db :db} [_ _ arguments]]
   (wrap-with-resolution db arguments :to execute-ethcall)))

(handlers/register-handler-fx
 :extensions/ethereum-erc20-total-supply
 (fn [{db :db} [_ _ {:keys [contract on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     "totalSupply()"
                        :outputs    ["uint256"]
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-ethcall))))

(handlers/register-handler-fx
 :extensions/ethereum-erc20-balance-of
 (fn [{db :db} [_ _ {:keys [contract token-owner on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     "balanceOf(address)"
                        :params     [token-owner]
                        :outputs    ["uint256"]
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-ethcall))))

(handlers/register-handler-fx
 :extensions/ethereum-erc20-allowance
 (fn [{db :db} [_ _ {:keys [contract token-owner spender on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     "allowance(address,address)"
                        :params     [token-owner spender]
                        :outputs    ["uint256"]
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-ethcall))))

(handlers/register-handler-fx
 :extensions/ethereum-erc20-transfer
 (fn [{db :db} [_ _ {:keys [contract to value on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     "transfer(address,uint256)"
                        :params     [to value]
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-send-transaction))))

(handlers/register-handler-fx
 :extensions/ethereum-erc20-transfer-from
 (fn [{db :db} [_ _ {:keys [contract from to value on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     "transferFrom(address,address,uint256)"
                        :params     [from to value]
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-send-transaction))))

(handlers/register-handler-fx
 :extensions/ethereum-erc20-approve
 (fn [{db :db} [_ _ {:keys [contract spender value on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     "approve(address,uint256)"
                        :params     [spender value]
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-send-transaction))))

(handlers/register-handler-fx
 :extensions/ethereum-erc721-owner-of
 (fn [{db :db} [_ _ {:keys [contract token-id on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     "ownerOf(uint256)"
                        :params     [token-id]
                        :outputs    ["address"]
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-ethcall))))

(handlers/register-handler-fx
 :extensions/ethereum-erc721-is-approved-for-all
 (fn [{db :db} [_ _ {:keys [contract owner operator on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     "isApprovedForAll(address,address)"
                        :params     [owner operator]
                        :outputs    ["bool"]
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-ethcall))))

(handlers/register-handler-fx
 :extensions/ethereum-erc721-get-approved
 (fn [{db :db} [_ _ {:keys [contract token-id on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     "getApproved(uint256)"
                        :params     [token-id]
                        :outputs    ["address"]
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-ethcall))))

(handlers/register-handler-fx
 :extensions/ethereum-erc721-set-approval-for-all
 (fn [{db :db} [_ _ {:keys [contract to approved on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     "setApprovalForAll(address,bool)"
                        :params     [to approved]
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-send-transaction))))

(handlers/register-handler-fx
 :extensions/ethereum-erc721-safe-transfer-from
 (fn [{db :db} [_ _ {:keys [contract from to token-id data on-success on-failure]}]]
   (let [json-rpc-args {:to contract
                        :method     (if data
                                      "safeTransferFrom(address,address,uint256,bytes)"
                                      "safeTransferFrom(address,address,uint256)")
                        :params     (if data
                                      [from to token-id data]
                                      [from to token-id])
                        :on-success on-success
                        :on-failure on-failure}]
     (wrap-with-resolution db json-rpc-args :to execute-send-transaction))))

(defn- event-topic-enc [event params]
  (let [eventid (str event "(" (string/join "," params) ")")]
    (abi-spec/sha3 eventid)))

(defn- get-no-indexed
  "Return a vector without indexed elements"
  [X indexed]
  (as-> (into [] (take (count X) (range))) $
    (zipmap $ X)
    (into [] (vals (apply dissoc $ indexed)))))

(defn- get-indexed
  "Return a vector with indexed elements"
  [X indexed]
  (let [keys (into [] (take (count X) (range)))]
    (mapv #((zipmap keys X) %) indexed)))

(defn- get-hint
  "Return the hint with the specified event (Topic0) from a vector of event hints given by the user"
  [hints first-topic]
  (as-> hints $
    (mapv #(event-topic-enc (% :event) (% :params)) $)
    (.indexOf $ first-topic)
    (get hints $)))

(defn- decode-data
  "Return a map with all data params decoded"
  [data hint]
  (let [indexes (hint :indexed)
        params (get-no-indexed (hint :params) indexes)
        ;; Exclude indexed params and names from decode in data,
        ;; these are decoded in topics
        names (mapv keyword (get-no-indexed (hint :names) indexes))
        data-values (mapv (partial apply str) (partition 64 (string/replace-first data #"0x" "")))]
    (zipmap names (mapv abi-spec/hex-to-value data-values params))))

(defn- decode-topics
  "This assumes that Topic 0 is filtered and return a map with all topics decoded"
  [topics hint]
  ;; Solidity indexed event params may number up to 3 (max 4 Topics)
  (zipmap [:topic1 :topic2 :topic3] (mapv abi-spec/hex-to-value topics (get-indexed (hint :params) (hint :indexed)))))

(defn- flatten-input
  "Flatten the input of event provided by an developer of extensions and return a 1 depth vector"
  [input]
  (into [] (flatten input)))

(defn parse-log [events-hints {:keys [address transactionHash blockHash transactionIndex topics blockNumber logIndex removed data]}]
  (merge {:data data
          :topics  topics
          :address address
          :removed removed}

         ;; filter useless topic 0 and decode topics
         (when (and topics (seq events-hints)) {:topics (decode-topics (filterv #(not (= (first topics) %)) topics) (get-hint events-hints (first topics)))})
         (when (and data (seq events-hints)) {:data (decode-data data (get-hint events-hints (first topics)))})
         (when logIndex {:log-index (abi-spec/hex-to-number logIndex)})
         (when transactionIndex {:transaction-index (abi-spec/hex-to-number transactionIndex)})
         (when transactionHash {:transaction-hash transactionHash})
         (when blockHash {:block-hash blockHash})
         (when blockNumber {:block-number (abi-spec/hex-to-number blockNumber)})))

(defn- parse-receipt [events-hints m]
  (when m
    (let [{:keys [status transactionHash transactionIndex blockHash blockNumber from to cumulativeGasUsed gasUsed contractAddress logs logsBloom]} m]
      {:status              (= 1 (abi-spec/hex-to-number status))
       :transaction-hash    transactionHash
       :transaction-index   (abi-spec/hex-to-number transactionIndex)
       :block-hash          blockHash
       :block-number        (abi-spec/hex-to-number blockNumber)
       :from                from
       :to                  to
       :cumulative-gas-used (abi-spec/hex-to-number cumulativeGasUsed)
       :gas-used            (abi-spec/hex-to-number gasUsed)
       :contract-address    contractAddress
       :logs                (map #(parse-log events-hints %) logs)
       :logs-bloom          logsBloom})))

(handlers/register-handler-fx
 :extensions/ethereum-transaction-receipt
 (fn [_ [_ _ {:keys [value] :as m}]]
   (rpc-call constants/web3-transaction-receipt [value] #(parse-receipt (flatten-input (:topics-hints m)) %) m)))

(handlers/register-handler-fx
 :extensions/ethereum-await-transaction-receipt
 (fn [_ [_ _ {:keys [value interval on-success] :as m}]]
   (let [id            (atom nil)
         new-on-success (fn [o] (js/clearInterval @id) (on-success o))]
     (reset! id (js/setInterval #(rpc-call constants/web3-transaction-receipt [value] (fn [result] (parse-receipt (flatten-input (:topics-hints m)) result))
                                           (assoc m :on-success new-on-success)) interval))
     nil)))

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
  (mapv #(str "0x" (abi-spec/enc {:type (types-mapping type) :value %})) values))

(defn- generate-topic [t]
  (cond
    (or (nil? t) (string? t)) t ;; nil topic ;; immediate topic (extension encode topic by its own) ;; vector of immediate topics
    (vector? t) (mapv generate-topic t) ;; descend in vector elements
    (map? t) ;; simplified topic interface, we need to encode
    (let [{:keys [event params type values]} t]
      (cond
        (some? event) (event-topic-enc event params);; event params topic {:event "Transfer" :params ["uint"]}
        (some? type) (values-topic-enc type values) ;; indexed values topic
        :else nil)) ;; error
    :else nil))

(defn- ensure-hex-bn [block]
  (cond
    (nil? block) block
    (re-matches #"^[0-9]+$" block) (str "0x" (abi-spec/number-to-hex block))
    :else block))

(defn- execute-get-logs [_ {:keys [from to address topics block-hash] :as m}]
  (let [params [{:fromBlock      (ensure-hex-bn from)
                 :toBlock        (ensure-hex-bn to)
                 :address   address
                 :topics    (generate-topic topics)
                 :blockhash block-hash}]]

    (rpc-call constants/web3-get-logs params (fn [results] (map #(parse-log (flatten-input (:topics m)) %) results)) m))) ; Get event-hints from the topics input given by the user

(handlers/register-handler-fx
 :extensions/ethereum-logs
 (fn [{db :db} [_ _ arguments]]
   (wrap-with-resolution db arguments :address execute-get-logs)))

(handlers/register-handler-fx
 :extensions/ethereum-resolve-ens
 (fn [{db :db} [_ _ {:keys [name on-success on-failure]}]]
   (if (ens/is-valid-eth-name? name)
     (let [{:keys [network]} db
           network-info (get-in db [:account/account :networks network])
           chain (ethereum/network->chain-keyword network-info)
           registry (get ens/ens-registries chain)]
       (ens/get-addr registry name
                     #(on-success {:value %})))
     (when on-failure
       (on-failure {:value (str "'" name "' is not a valid name")})))))

;; EXTENSION SIGN -> SIGN MESSAGE
(handlers/register-handler-fx
 :extensions/ethereum-sign
 (fn [{db :db :as cofx} [_ _ {:keys [message data id on-success on-failure]}]]
   (if (and message data)
     (when on-failure
       (on-failure {:error "only one of :message and :data can be used"}))
     (fx/merge cofx
               {:db (assoc-in db [:wallet :send-transaction]
                              {:id                id
                               :from             (ethereum/normalized-address (get-in db [:account/account :address]))
                               :data             (or data (str "0x" (abi-spec/from-utf8 message)))
                               :on-result        [:extensions/wallet-ui-on-success on-success]
                               :on-error         [:extensions/wallet-ui-on-failure on-failure]
                               :method           constants/web3-personal-sign})}
               (navigation/navigate-to-cofx :wallet-sign-message-modal nil)))))

(handlers/register-handler-fx
 :extensions/ethereum-create-address
 (fn [_ [_ _ {:keys [on-result]}]]
   (let [args {:jsonrpc "2.0"
               :method constants/status-create-address}
         payload (types/clj->json args)]
     (status/call-private-rpc payload #(let [{:keys [error result]} (types/json->clj %1)
                                             response (if error {:result result :error error}
                                                          {:result result})]
                                         (on-result response))))))

;; poll logs implementation
(handlers/register-handler-fx
 :extensions/ethereum-logs-changes
 (fn [_ [_ _ {:keys [id] :as m}]]
   (rpc-call constants/web3-get-filter-changes [(abi-spec/number-to-hex id)] (fn [results] (map #(parse-log (flatten-input (:topics-hints m)) %) results)) m)))

(handlers/register-handler-fx
 :extensions/ethereum-cancel-filter
 (fn [_ [_ _ {:keys [id] :as m}]]
   (rpc-call constants/web3-uninstall-filter [(abi-spec/number-to-hex id)] #(abi-spec/hex-to-value % "bool") m)))

(defn create-filter-method [type]
  (case type
    :filter              constants/web3-new-filter
    :block               constants/web3-new-block-filter
    :pending-transaction constants/web3-new-pending-transaction-filter))

(defn create-filter-arguments [type {:keys [from to address block-hash topics]}]
  (case type
    :filter
    [{:fromBlock (ensure-hex-bn from)
      :toBlock   (ensure-hex-bn to)
      :address   address
      :topics    (mapv generate-topic topics)
      :blockhash block-hash}]))

(handlers/register-handler-fx
 :extensions/ethereum-create-filter
 (fn [_ [_ _ {:keys [type] :as m}]]
   (rpc-call (create-filter-method type) (create-filter-arguments type m) abi-spec/hex-to-number m)))

(handlers/register-handler-fx
 :extensions/shh-post
 (fn [_ [_ _ {:keys [from to topics payload priority ttl] :as m}]]
   (let [params [{:from      from
                  :to        to
                  :topics    (generate-topic topics)
                  :payload   payload
                  :priority  (abi-spec/number-to-hex priority)
                  :ttl       (abi-spec/number-to-hex ttl)}]]
     (rpc-call constants/web3-shh-post params #(abi-spec/hex-to-value % "bool") m))))

(handlers/register-handler-fx
 :extensions/shh-new-identity
 (fn [_ [_ _ arguments]]
   (let [params []]
     (rpc-call constants/web3-shh-new-identity params #(identity  %) arguments))))

(handlers/register-handler-fx
 :extensions/shh-has-identity
 (fn [_ [_ _ {:keys [address] :as m}]]
   ((rpc-call constants/web3-shh-has-identity address #(abi-spec/hex-to-value % "bool") m))))

(handlers/register-handler-fx
 :extensions/shh-new-group
 (fn [_ [_ _ arguments]]
   (let [params []]
     (rpc-call constants/web3-shh-new-group params #(identity  %) arguments))))

(handlers/register-handler-fx
 :extensions/shh-add-to-group
 (fn [_ [_ _ {:keys [address] :as m}]]
   ((rpc-call constants/web3-shh-add-to-group address #(abi-spec/hex-to-value % "bool") m))))

(handlers/register-handler-fx
 :extensions/shh-new-filter
 (fn [_ [_ _ {:keys [to topics] :as m}]]
   (let [params [{:to        to
                  :topics    (generate-topic topics)}]]
     (rpc-call constants/web3-shh-new-filter params abi-spec/hex-to-number m))))

(handlers/register-handler-fx
 :extensions/shh-uninstall-filter
 (fn [_ [_ _ {:keys [id] :as m}]]
   (rpc-call constants/web3-shh-uninstall-filter [(abi-spec/number-to-hex id)] #(abi-spec/hex-to-value % "bool") m)))

(defn- parse-messages [{:keys [hash from to expiry ttl sent topics payload workProved]}]
  {:hash          hash
   :from          from
   :to            to
   :expiry        (abi-spec/hex-to-number expiry)
   :ttl           (abi-spec/hex-to-number ttl)
   :sent          (abi-spec/hex-to-number sent)
   :topics        topics
   :payload       payload
   :workProved    (abi-spec/hex-to-number workProved)})

(handlers/register-handler-fx
 :extensions/shh-get-filter-changes
 (fn [_ [_ _ {:keys [id] :as m}]]
   (rpc-call constants/web3-shh-get-filter-changes [(abi-spec/number-to-hex id)] #(map parse-messages %) m)))

(handlers/register-handler-fx
 :extensions/shh-get-messages
 (fn [_ [_ _ {:keys [id] :as m}]]
   (rpc-call constants/web3-shh-get-messages [(abi-spec/number-to-hex id)] #(map parse-messages %) m)))
