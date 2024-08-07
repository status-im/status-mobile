(ns status-im.contexts.wallet.wallet-connect.core
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils.networks :as networks]
            [utils.security.core :as security]
            [utils.string]
            [utils.transforms :as transforms]))

(def method-to-screen
  {constants/wallet-connect-personal-sign-method        :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-sign-typed-method       :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-sign-method             :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-sign-typed-v4-method    :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-send-transaction-method :screen/wallet-connect.send-transaction
   constants/wallet-connect-eth-sign-transaction-method :screen/wallet-connect.sign-transaction})

(defn extract-native-call-signature
  [data]
  (-> data transforms/json->clj :result))

(defn chain-id->eip155
  [chain-id]
  (str "eip155:" chain-id))

(defn eip155->chain-id
  [chain-id-str]
  (-> chain-id-str
      (string/split #":")
      last
      edn/read-string))

(defn format-eip155-address
  [address chain-id]
  (str chain-id ":" address))

(defn get-request-method
  [event]
  (get-in event [:params :request :method]))

(defn get-request-params
  [event]
  (get-in event [:params :request :params]))

(defn get-db-current-request-event
  [db]
  (get-in db [:wallet-connect/current-request :event]))

(defn get-session-dapp-metadata
  [proposal]
  (get-in proposal [:params :proposer :metadata]))

(defn get-db-current-request-params
  [db]
  (-> db
      get-db-current-request-event
      get-request-params))

(def ^:private sign-typed-data-by-version
  {:v1 native-module/sign-typed-data
   :v4 native-module/sign-typed-data-v4})

(defn sign-typed-data
  [version data address password]
  (let [f (get sign-typed-data-by-version version)]
    (->> password
         security/safe-unmask-data
         (f data address))))

(defn get-proposal-networks
  [proposal]
  (let [required-namespaces (get-in proposal [:params :requiredNamespaces])
        optional-namespaces (get-in proposal [:params :optionalNamespaces])]
    (->> [required-namespaces optional-namespaces]
         (map #(get-in % [:eip155 :chains]))
         (apply concat)
         (into #{}))))

(defn proposal-networks-intersection
  [proposal supported-networks]
  (let [proposed-networks (get-proposal-networks proposal)]
    (->> supported-networks
         (filter #(->> %
                       chain-id->eip155
                       (contains? proposed-networks))))))

(defn required-networks-supported?
  [proposal supported-networks]
  (let [supported-namespaces #{:eip155}
        required-namespaces  (get-in proposal [:params :requiredNamespaces])]
    (when (every? #(contains? supported-namespaces %)
                  (keys required-namespaces))
      (let [required-networks (get-in required-namespaces [:eip155 :chains])
            supported-eip155  (set (map chain-id->eip155 supported-networks))]
        (every? #(contains? supported-eip155 %)
                required-networks)))))

(defn get-networks-by-mode
  [db]
  (let [test-mode? (get-in db [:profile/profile :test-networks-enabled?])
        networks   (get-in db [:wallet :networks (if test-mode? :test :prod)])]
    (mapv #(-> % :chain-id) networks)))

(defn- add-full-testnet-name
  "Updates the `:full-name` key with the full testnet name if using testnet `:chain-id`.\n
  e.g. `{:full-name \"Mainnet\"}` -> `{:full-name \"Mainnet Sepolia\"`}`"
  [network]
  (let [add-testnet-name (fn [testnet-name]
                           (update network :full-name #(str % " " testnet-name)))]
    (condp #(contains? %1 %2) (:chain-id network)
      constants/sepolia-chain-ids (add-testnet-name constants/sepolia-full-name)
      constants/goerli-chain-ids  (add-testnet-name constants/goerli-full-name)
      network)))

(defn chain-id->network-details
  [chain-id]
  (-> chain-id
      (networks/get-network-details)
      (add-full-testnet-name)))

(defn session-networks-allowed?
  [testnet-mode? {:keys [chains]}]
  (let [chain-ids (set (map (fn [chain]
                              (-> chain
                                  (string/split ":")
                                  second
                                  js/parseInt))
                            chains))]
    (if testnet-mode?
      (set/subset? chain-ids (set/union constants/sepolia-chain-ids constants/goerli-chain-ids))
      (set/subset? chain-ids constants/mainnet-chain-ids))))

(defn event-should-be-handled?
  [db {:keys [topic]}]
  (let [testnet-mode? (get-in db [:profile/profile :test-networks-enabled?])]
    (some #(and (= (:topic %) topic)
                (session-networks-allowed? testnet-mode? %))
          (:wallet-connect/sessions db))))

(defn sdk-session->db-session
  [{:keys [topic expiry pairingTopic] :as session}]
  {:topic        topic
   :expiry       expiry
   :sessionJson  (transforms/clj->json session)
   :pairingTopic pairingTopic
   :name         (get-in session [:peer :metadata :name])
   :iconUrl      (get-in session [:peer :metadata :icons 0])
   :url          (get-in session [:peer :metadata :url])
   :accounts     (get-in session [:namespaces :eip155 :accounts])
   :chains       (get-in session [:namespaces :eip155 :chains])
   :disconnected false})

(defn filter-operable-accounts
  [accounts]
  (filter #(and (:operable? %)
                (not (:watch-only? %)))
          accounts))

(defn filter-sessions-for-account-addresses
  [account-addresses sessions]
  (filter (fn [{:keys [accounts]}]
            (some (fn [account]
                    (some (fn [account-address]
                            (clojure.string/includes? account account-address))
                          account-addresses))
                  accounts))
          sessions))

(defn compute-dapp-name
  "Sometimes dapps have no name or an empty name. Return url as name in that case"
  [name url]
  (if (seq name)
    name
    (when (seq url)
      (-> url
          utils.string/remove-trailing-slash
          utils.string/remove-http-prefix
          string/capitalize))))

(defn compute-dapp-icon-path
  "Some dapps have icons with relative paths, make paths absolute in those cases, send nil if icon is missing"
  [icon-path url]
  (when (and (seq icon-path)
             (seq url))
    (if (string/starts-with? icon-path "http")
      icon-path
      (str (utils.string/remove-trailing-slash url) icon-path))))
