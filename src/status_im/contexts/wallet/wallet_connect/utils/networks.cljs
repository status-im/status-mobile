(ns status-im.contexts.wallet.wallet-connect.utils.networks
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as string]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils.networks :as networks]
            [utils.string]))

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

(defn- add-full-testnet-name
  "Updates the `:full-name` key with the full testnet name if using testnet `:chain-id`.\n
  e.g. `{:full-name \"Mainnet\"}` -> `{:full-name \"Mainnet Sepolia\"`}`"
  [network]
  (let [add-testnet-name (fn [testnet-name]
                           (update network :full-name #(str % " " testnet-name)))]
    (condp #(contains? %1 %2) (:chain-id network)
      constants/sepolia-chain-ids (add-testnet-name constants/sepolia-full-name)
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
      (set/subset? chain-ids constants/sepolia-chain-ids)
      (set/subset? chain-ids constants/mainnet-chain-ids))))

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

(defn event-should-be-handled?
  [db {:keys [topic]}]
  (let [testnet-mode? (get-in db [:profile/profile :test-networks-enabled?])]
    (some #(and (= (:topic %) topic)
                (session-networks-allowed? testnet-mode? %))
          (:wallet-connect/sessions db))))
