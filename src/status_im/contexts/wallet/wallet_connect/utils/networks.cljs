(ns status-im.contexts.wallet.wallet-connect.utils.networks
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as string]
            [status-im.contexts.wallet.networks.db :as networks.db]
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

(defn session-networks-allowed?
  [supported-chain-ids {:keys [chains]}]
  (let [session-chain-ids (set (map (fn [chain]
                                      (-> chain
                                          (string/split ":")
                                          second
                                          js/parseInt))
                                    chains))]
    (set/subset? session-chain-ids supported-chain-ids)))

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

(defn event-should-be-handled?
  [db {:keys [topic]}]
  (let [chain-ids (networks.db/get-chain-ids db)]
    (some #(and (= (:topic %) topic)
                (session-networks-allowed? chain-ids %))
          (:wallet-connect/sessions db))))
