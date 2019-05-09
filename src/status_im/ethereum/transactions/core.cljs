(ns status-im.ethereum.transactions.core
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            re-frame.db
            [status-im.utils.async :as async-util]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.fx :as fx]
            [status-im.utils.http :as http]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(def sync-interval-ms 15000)
(def sync-timeout-ms  20000)
(def confirmations-count-threshold 12)

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
        (str "https://" network-subdomain "etherscan.io/tx/" hash)))))

(def etherscan-api-key "DMSI4UAAKUBVGCDMVP3H2STAMSAUV7BYFI")

(defn- get-api-network-subdomain [chain]
  (case chain
    (:testnet) "api-ropsten"
    (:mainnet) "api"
    (:rinkeby) "api-rinkeby"))

(defn- get-transaction-url
  ([chain account] (get-transaction-url chain account false))
  ([chain account chaos-mode?]
   {:pre  [(keyword? chain) (string? account)]
    :post [(string? %)]}
   (let [network-subdomain (get-api-network-subdomain chain)]
     (if chaos-mode?
       "http://httpstat.us/500"
       (str "https://" network-subdomain
            ".etherscan.io/api?module=account&action=txlist&address=0x"
            account "&startblock=0&endblock=99999999&sort=desc&apikey=" etherscan-api-key "&q=json")))))

(defn- format-transaction [account
                           {:keys [value timeStamp blockNumber hash from to
                                   gas gasPrice gasUsed nonce input isError]}]
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
     :data          input}))

(defn- format-transactions-response [response account]
  (let [{:keys [result]} (types/json->clj response)]
    (cond-> {}
      (vector? result)
      (into (comp
             (map (partial format-transaction account))
             (map (juxt :hash identity)))
            result))))

(defn- etherscan-transactions
  ([chain account on-success on-error]
   (etherscan-transactions chain account on-success on-error false))
  ([chain account on-success on-error chaos-mode?]
   (if (etherscan-supported? chain)
     (let [url (get-transaction-url chain account chaos-mode?)]
       (log/debug "HTTP GET" url)
       (http/get url
                 #(on-success (format-transactions-response % account))
                 on-error))
     (log/info "Etherscan not supported for " chain))))

(defn- get-transactions [{:keys [web3 chain chain-tokens account-address
                                 success-fn error-fn chaos-mode?]}]
  (log/debug "Syncing transactions data..")
  (etherscan-transactions chain
                          account-address
                          success-fn
                          error-fn
                          chaos-mode?))

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

(letfn [(combine-entries [transaction token-transfer]
          (merge transaction (select-keys token-transfer [:symbol :from :to :value :type :token :transfer])))
        (tx-and-transfer? [tx1 tx2]
                          (and (not (:transfer tx1)) (:transfer tx2)))
        (both-transfer?
         [tx1 tx2]
         (and (:transfer tx1) (:transfer tx2)))]
  (defn- dedupe-transactions [tx1 tx2]
    (cond (tx-and-transfer? tx1 tx2) (combine-entries tx1 tx2)
          (tx-and-transfer? tx2 tx1) (combine-entries tx2 tx1)
          :else tx2)))

;; ----------------------------------------------------------------------------
;; The following Code represents how fetching transactions is
;; complected with the rest of the application
;; ----------------------------------------------------------------------------

(defonce polling-executor (atom nil))

(defn transactions-query-helper [web3 all-tokens account-address chain done-fn chaos-mode?]
  (get-transactions
   {:account-address account-address
    :chain           chain
    :chain-tokens (into {} (map (juxt :address identity) (tokens/tokens-for all-tokens chain)))
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
                  (done-fn))
    :chaos-mode? chaos-mode?}))

(defn- sync-now! [{:keys [network-status :account/account :wallet/all-tokens app-state network web3] :as opts}]
  (when @polling-executor
    (let [chain (ethereum/network->chain-keyword (get-in account [:networks network]))
          account-address (:address account)
          chaos-mode? (get-in account [:settings :chaos-mode?])]
      (when (and (not= network-status :offline)
                 (= app-state "active")
                 (not= :custom chain))
        (async-util/async-periodic-run!
         @polling-executor
         #(transactions-query-helper web3 all-tokens account-address chain % chaos-mode?))))))

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
            transaction-ids (set (keys transaction-map))
            chaos-mode? (get-in account [:settings :chaos-mode?])]
        (if-not (not-empty (set/difference chat-transaction-ids transaction-ids))
          (done-fn)
          (transactions-query-helper web3 all-tokens account-address chain done-fn chaos-mode?))))))

(defn- start-sync! [{:keys [:account/account network web3] :as options}]
  (let [account-address (:address account)]
    (when @polling-executor
      (async-util/async-periodic-stop! @polling-executor))
    (reset! polling-executor
            (async-util/async-periodic-exec
             (partial #'background-sync web3 account-address)
             sync-interval-ms
             sync-timeout-ms)))
  (sync-now! options))

(re-frame/reg-fx
 ::sync-transactions-now
 (fn [db] (sync-now! db)))

(re-frame/reg-fx
 ::start-sync-transactions
 (fn [db] (start-sync! db)))

(fx/defn start-sync [{:keys [db]}]
  {::start-sync-transactions
   (select-keys db [:network-status :account/account :wallet/all-tokens
                    :app-state :network :web3])})

(re-frame/reg-fx
 ::stop-sync-transactions
 #(when @polling-executor
    (async-util/async-periodic-stop! @polling-executor)))

(fx/defn stop-sync [_]
  {::stop-sync-transactions nil})
