(ns status-im.models.transactions
  (:require [clojure.set :as set]
            [cljs.core.async :as async]
            [clojure.string :as string]
            [status-im.utils.async :as async-util]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.constants :as constants]
            [status-im.native-module.core :as status]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.http :as http]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]
            [re-frame.db])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(def sync-interval-ms 15000)
(def sync-timeout-ms  20000)
(def confirmations-count-threshold 12)
(def block-query-limit 100000)

;; ----------------------------------------------------------------------------
;; token transfer event logs from eth-node
;; ----------------------------------------------------------------------------

(defn- parse-json [s]
  {:pre [(string? s)]}
  (try
    (let [res (-> s
                  js/JSON.parse
                  (js->clj :keywordize-keys true))]
      (if (= (:error res) "")
        {:result true}
        res))
    (catch :default e
      {:error (.-message e)})))

(defn- add-padding [address]
  {:pre [(string? address)]}
  (str "0x000000000000000000000000" (subs address 2)))

(defn- remove-padding [topic]
  {:pre [(string? topic)]}
  (str "0x" (subs topic 26)))

(defn- parse-transaction-entries [current-block-number block-info chain-tokens direction transfers]
  {:pre [(integer? current-block-number) (map? block-info)
         (map? chain-tokens) (every? (fn [[k v]] (and (string? k) (map? v))) chain-tokens)
         (keyword? direction)
         (every? map? transfers)]}
  (into {}
        (keep identity
              (for [transfer transfers]
                (when-let [token (->> transfer :address (get chain-tokens))]
                  (when-not (:nft? token)
                    [(:transactionHash transfer)
                     {:block         (-> block-info :number str)
                      :hash          (:transactionHash transfer)
                      :symbol        (:symbol token)
                      :from          (some-> transfer :topics second remove-padding)
                      :to            (some-> transfer :topics last remove-padding)
                      :value         (-> transfer :data ethereum/hex->bignumber)
                      :type          direction

                      :confirmations (str (- current-block-number (-> transfer :blockNumber ethereum/hex->int)))

                      :gas-price     nil
                      :nonce         nil
                      :data          nil

                      :gas-limit     nil
                      :timestamp     (-> block-info :timestamp (* 1000) str)

                      :gas-used      nil

                      ;; NOTE(goranjovic) - metadata on the type of token: contains name, symbol, decimas, address.
                      :token         token

                      ;; NOTE(goranjovic) - if an event has been emitted, we can say there was no error
                      :error?        false

                      ;; NOTE(goranjovic) - just a flag we need when we merge this entry with the existing entry in
                      ;; the app, e.g. transaction info with gas details, or a previous transfer entry with old
                      ;; confirmations count.
                      :transfer      true}]))))))

(defn- add-block-info [web3 current-block-number chain-tokens direction result success-fn]
  {:pre [web3 (integer? current-block-number) (map? chain-tokens) (keyword? direction)
         (every? map? result)
         (fn? success-fn)]}
  (let [transfers-by-block (group-by :blockNumber result)]
    (doseq [[block-number transfers] transfers-by-block]
      (ethereum/get-block-info web3 (ethereum/hex->int block-number)
                               (fn [block-info]
                                 (if-not (map? block-info)
                                   (log/error "Request for block info failed")
                                   (success-fn (parse-transaction-entries current-block-number
                                                                          block-info
                                                                          chain-tokens
                                                                          direction
                                                                          transfers))))))))

(defn- response-handler [web3 current-block-number chain-tokens direction error-fn success-fn]
  (fn handle-response
    ([response]
     #_(log/debug "Token transaction logs recieved --" (pr-str response))
     (let [{:keys [error result]} (parse-json response)]
       (handle-response error result)))
    ([error result]
     (if error
       (error-fn error)
       (add-block-info web3 current-block-number chain-tokens direction result success-fn)))))

(defn- limited-from-block [current-block-number]
  {:pre [(integer? current-block-number)]
   ;; needs to be a positive etherium hex
   :post [(string? %) (string/starts-with? % "0x")]}
  (-> current-block-number (- block-query-limit) (max 0) ethereum/int->hex))

;; Here we are querying event logs for Transfer events.
;;
;; The parameters are as follows:
;; - address - token smart contract address
;; - fromBlock - we need to specify it, since default is latest
;; - topics[0] - hash code of the Transfer event signature
;; - topics[1] - address of token sender with leading zeroes padding up to 32 bytes
;; - topics[2] - address of token sender with leading zeroes padding up to 32 bytes
;;

(defn- get-token-transfer-logs
  ;; NOTE(goranjovic): here we use direct JSON-RPC calls to get event logs because of web3 event issues with infura
  ;; we still use web3 to get other data, such as block info
  [web3 current-block-number chain-tokens direction address cb]
  {:pre [web3 (integer? current-block-number) (map? chain-tokens) (keyword? direction) (string? address) (fn? cb)]}
  (let [[from to] (if (= :inbound direction)
                    [nil (add-padding (ethereum/normalized-address address))]
                    [(add-padding (ethereum/normalized-address address)) nil])
        from-block (limited-from-block current-block-number)
        args {:jsonrpc "2.0"
              :id      2
              :method  constants/web3-get-logs
              :params  [{:address (keys chain-tokens)
                         :fromBlock from-block
                         :topics    [constants/event-transfer-hash from to]}]}
        payload (.stringify js/JSON (clj->js args))]
    (status/call-private-rpc payload
                             (response-handler web3 current-block-number chain-tokens direction ethereum/handle-error cb))))

(defn- get-token-transactions
  [web3 chain-tokens direction address cb]
  {:pre [web3 (map? chain-tokens) (keyword? direction) (string? address) (fn? cb)]}
  (ethereum/get-block-number web3
                             #(get-token-transfer-logs web3 % chain-tokens direction address cb)))

;; --------------------------------------------------------------------------
;; etherscan transactions
;; --------------------------------------------------------------------------

(def etherscan-supported? #{:testnet :mainnet :rinkeby})

(let [network->subdomain {:testnet "ropsten" :rinkeby "rinkeby"}]
  (defn get-transaction-details-url [chain hash]
    {:pre [(keyword? chain) (string? hash)]
     :post [(or (nil? %) (string? %))]}
    (when (etherscan-supported? chain)
      (let [network-subdomain (when-let [subdomain (network->subdomain chain)]
                                (str subdomain "."))]
        (str "https://" network-subdomain  "etherscan.io/tx/" hash)))))

(def etherscan-api-key "DMSI4UAAKUBVGCDMVP3H2STAMSAUV7BYFI")

(defn- get-api-network-subdomain [chain]
  (case chain
    (:testnet) "api-ropsten"
    (:mainnet) "api"
    (:rinkeby) "api-rinkeby"))

(defn- get-transaction-url [chain account]
  {:pre [(keyword? chain) (string? account)]
   :post [(string? %)]}
  (let [network-subdomain (get-api-network-subdomain chain)]
    (str "https://" network-subdomain ".etherscan.io/api?module=account&action=txlist&address=0x"
         account "&startblock=0&endblock=99999999&sort=desc&apikey=" etherscan-api-key "&q=json")))

(defn- format-transaction [account
                           {:keys [value timeStamp blockNumber hash from to
                                   gas gasPrice gasUsed nonce confirmations
                                   input isError]}]
  (let [inbound? (= (str "0x" account) to)
        error?   (= "1" isError)]
    {:value         value
     ;; timestamp is in seconds, we convert it in ms
     :timestamp     (str timeStamp "000")
     :symbol        :ETH
     :type          (cond error?   :failed
                          inbound? :inbound
                          :else    :outbound)
     :block         blockNumber
     :hash          hash
     :from          from
     :to            to
     :gas-limit     gas
     :gas-price     gasPrice
     :gas-used      gasUsed
     :nonce         nonce
     :confirmations confirmations
     :data          input}))

(defn- format-transactions-response [response account]
  (let [{:keys [result]} (types/json->clj response)]
    (cond-> {}
      (vector? result)
      (into (comp
             (map (partial format-transaction account))
             (map (juxt :hash identity)))
            result))))

(defn- etherscan-transactions [chain account on-success on-error]
  (if (etherscan-supported? chain)
    (let [url (get-transaction-url chain account)]
      (log/debug "HTTP GET" url)
      (http/get url
                #(on-success (format-transactions-response % account))
                on-error))
    (log/info "Etherscan not supported for " chain)))

(defn- get-transactions [{:keys [web3 chain chain-tokens account-address success-fn error-fn]}]
  (log/debug "Syncing transactions data..")
  (etherscan-transactions chain
                          account-address
                          success-fn
                          error-fn)
  (doseq [direction [:inbound :outbound]]
    (get-token-transactions web3
                            chain-tokens
                            direction
                            account-address
                            success-fn)))

;; ---------------------------------------------------------------------------
;; Periodic background job
;; ---------------------------------------------------------------------------

(defn- async-periodic-run!
  ([async-periodic-chan]
   (async-periodic-run! async-periodic-chan true))
  ([async-periodic-chan worker-fn]
   (async/put! async-periodic-chan worker-fn)))

(defn- async-periodic-stop! [async-periodic-chan]
  (async/close! async-periodic-chan))

(defn- async-periodic-exec
  "Periodically execute an function.
  Takes a work-fn of one argument `finished-fn -> any` this function
  is passed a finished-fn that must be called to signal that the work
  being performed in the work-fn is finished.

  The work-fn can be forced to run immediately "
  [work-fn interval-ms timeout-ms]
  {:pre [(fn? work-fn) (integer? interval-ms) (integer? timeout-ms)]}
  (let [do-now-chan (async/chan (async/sliding-buffer 1))]
    (go-loop []
      (let [timeout (async-util/timeout interval-ms)
            finished-chan (async/promise-chan)
            [v ch] (async/alts! [do-now-chan timeout])
            worker (if (and (= ch do-now-chan) (fn? v))
                     v work-fn)]
        (when-not (and (= ch do-now-chan) (nil? v))
          (try
            (worker #(async/put! finished-chan true))
            ;; if an error occurs in work-fn log it and consider it done
            (catch :default e
              (log/error "failed to run transaction sync" e)
              (async/put! finished-chan true)))
          ;; sanity timeout for work-fn
          (async/alts! [finished-chan (async-util/timeout timeout-ms)])
          (recur))))
    do-now-chan))

;; -----------------------------------------------------------------------------
;; Helpers functions that help determine if a background sync should execute
;; -----------------------------------------------------------------------------

(defn- keyed-memoize
  "Space bounded memoize.

  Takes a key-function that decides the key in the cache for the
  memoized value. Takes a value function that will extract the value
  that will invalidate the cache if it changes.  And finally the
  function to memoize.

  Memoize that doesn't grow bigger than the number of keys."
  [key-fn val-fn f]
  (let [val-store (atom {})
        res-store (atom {})]
    (fn [arg]
      (let [k (key-fn arg)
            v (val-fn arg)]
        (if (not= (get @val-store k) v)
          (let [res (f arg)]
            #_(prn "storing!!!!" res)
            (swap! val-store assoc k v)
            (swap! res-store assoc k res)
            res)
          (get @res-store k))))))

;; Map[id, chat] -> Set[transaction-id]
;; chat may or may not have a :messages Map
(let [chat-map-entry->transaction-ids
      (keyed-memoize key (comp :messages val)
                     (fn [[_ chat]]
                       (some->> (:messages chat)
                                vals
                                (filter #(= "command" (:content-type %)))
                                (keep #(select-keys (get-in % [:content :params]) [:tx-hash :network])))))]
  (defn- chat-map->transaction-ids [network chat-map]
    {:pre  [(string? network) (every? map? (vals chat-map))]
     :post [(set? %)]}
    (let [network (string/replace network "_rpc" "")]
      (->> chat-map
           (remove (comp :public? val))
           (mapcat chat-map-entry->transaction-ids)
           (filter #(= network (:network %)))
           (map :tx-hash)
           set))))

;; Seq[transaction] -> truthy
(defn- have-unconfirmed-transactions?
  "Detects if some of the transactions have less than 12 confirmations"
  [transactions]
  {:pre [(every? string? (map :confirmations transactions))]}
  (->> transactions
       (map :confirmations)
       (map int)
       (some #(< % confirmations-count-threshold))))

(letfn [(combine-entries [transaction token-transfer]
          (merge transaction (select-keys token-transfer [:symbol :from :to :value :type :token :transfer])))
        (update-confirmations [tx1 tx2]
                              (assoc tx1 :confirmations (str (max (int (:confirmations tx1))
                                                                  (int (:confirmations tx2))))))
        (tx-and-transfer? [tx1 tx2]
                          (and (not (:transfer tx1)) (:transfer tx2)))
        (both-transfer?
         [tx1 tx2]
         (and (:transfer tx1) (:transfer tx2)))]
  (defn- dedupe-transactions [tx1 tx2]
    (cond (tx-and-transfer? tx1 tx2) (combine-entries tx1 tx2)
          (tx-and-transfer? tx2 tx1) (combine-entries tx2 tx1)
          (both-transfer? tx1 tx2)   (update-confirmations tx1 tx2)
          :else tx2)))

;; ----------------------------------------------------------------------------
;; The following Code represents how fetching transactions is
;; complected with the rest of the application
;; ----------------------------------------------------------------------------

(defonce polling-executor (atom nil))

(defn transactions-query-helper [web3 all-tokens account-address chain done-fn]
  (get-transactions
   {:account-address account-address
    :chain           chain
    :chain-tokens    (into {} (map (juxt :address identity) (tokens/tokens-for all-tokens chain)))
    :web3            web3
    :success-fn (fn [transactions]
                  #_(log/debug "Transactions received: " (pr-str (keys transactions)))
                  (swap! re-frame.db/app-db
                         (fn [app-db]
                           (when (= (get-in app-db [:account/account :address])
                                    account-address)
                             (update-in app-db
                                        [:wallet :transactions]
                                        #(merge-with dedupe-transactions % transactions)))))
                  (done-fn))
    :error-fn   (fn [http-error]
                  (log/debug "Unable to get transactions: " http-error)
                  (done-fn))}))

(defn- sync-now! [{:keys [network-status :account/account :wallet/all-tokens app-state network web3] :as opts}]
  (when @polling-executor
    (let [chain (ethereum/network->chain-keyword (get-in account [:networks network]))
          account-address (:address account)]
      (when (and (not= network-status :offline)
                 (= app-state "active")
                 (not= :custom chain))
        (async-periodic-run!
         @polling-executor
         (partial transactions-query-helper web3 all-tokens account-address chain))))))

;; this function handles background syncing of transactions
(defn- background-sync [web3 account-address done-fn]
  (let [{:keys [network network-status :account/account app-state wallet chats :wallet/all-tokens]} @re-frame.db/app-db
        chain (ethereum/network->chain-keyword (get-in account [:networks network]))]
    (assert (and web3 account-address network network-status account app-state wallet chats)
            "Must have all necessary data to run background transaction sync")
    (if-not (and (not= network-status :offline)
                 (= app-state "active")
                 (not= :custom chain))
      (done-fn)
      (let [chat-transaction-ids (chat-map->transaction-ids network chats)
            transaction-map (:transactions wallet)
            transaction-ids (set (keys transaction-map))]
        (if-not (or (have-unconfirmed-transactions? (vals transaction-map))
                    (not-empty (set/difference chat-transaction-ids transaction-ids)))
          (done-fn)
          (transactions-query-helper web3 all-tokens account-address chain done-fn))))))

(defn- start-sync! [{:keys [:account/account network web3] :as options}]
  (let [account-address (:address account)]
    (when @polling-executor
      (async-periodic-stop! @polling-executor))
    (reset! polling-executor
            (async-periodic-exec
             (partial #'background-sync web3 account-address)
             sync-interval-ms
             sync-timeout-ms)))
  (sync-now! options))

(re-frame/reg-fx
 ::sync-transactions-now
 (fn [db] (sync-now! db)))

(re-frame/reg-fx ::start-sync-transactions
                 (fn [db] (start-sync! db)))

(fx/defn start-sync [{:keys [db]}]
  {::start-sync-transactions (select-keys db [:network-status :account/account :app-state :network :web3])})

(re-frame/reg-fx
 ::stop-sync-transactions
 #(when @polling-executor
    (async-periodic-stop! @polling-executor)))

(fx/defn stop-sync [_]
  {::stop-sync-transactions nil})
