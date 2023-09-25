(ns status-im.ethereum.core
  (:require [clojure.string :as string]
            [status-im.ethereum.eip55 :as eip55]
            [native-module.core :as native-module]))

(defn sha3
  [s]
  (when s
    (native-module/sha3 (str s))))

(defn utf8-to-hex
  [s]
  (let [hex (native-module/utf8-to-hex (str s))]
    (if (empty? hex)
      nil
      hex)))

(defn hex-to-utf8
  [s]
  (let [utf8 (native-module/hex-to-utf8 s)]
    (if (empty? utf8)
      nil
      utf8)))

(def BSC-mainnet-chain-id 56)
(def BSC-testnet-chain-id 97)

;; IDs standardized in https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md#list-of-chain-ids
(def chains
  {:mainnet     {:id 1 :name "Mainnet"}
   :xdai        {:id 100 :name "xDai"}
   :goerli      {:id 5 :name "Goerli"}
   :bsc         {:id   BSC-mainnet-chain-id
                 :name "BSC"}
   :bsc-testnet {:id   BSC-testnet-chain-id
                 :name "BSC tetnet"}})

(defn chain-id->chain-keyword
  [i]
  (or (some #(when (= i (:id (val %))) (key %)) chains)
      :custom))

(defn chain-id->chain-name
  [i]
  (or (some #(when (= i (:id (val %))) (:name (val %))) chains)
      :custom))

(defn chain-keyword->chain-id
  [k]
  (get-in chains [k :id]))

(defn chain-keyword->snt-symbol
  [k]
  (case k
    :mainnet :SNT
    :STT))

(defn testnet?
  [id]
  (contains? #{(chain-keyword->chain-id :goerli)
               (chain-keyword->chain-id :bsc-testnet)}
             id))

(defn sidechain?
  [id]
  (contains? #{(chain-keyword->chain-id :xdai)
               (chain-keyword->chain-id :bsc)}
             id))

(defn network-with-upstream-rpc?
  [network]
  (get-in network [:config :UpstreamConfig :Enabled]))

(def hex-prefix "0x")

(defn normalized-hex
  [hex]
  (when hex
    (if (string/starts-with? hex hex-prefix)
      hex
      (str hex-prefix hex))))

(defn current-address
  [db]
  (-> (get-in db [:profile/profile :address])
      normalized-hex))

(defn get-default-account
  [accounts]
  (some #(when (:wallet %) %) accounts))

(defn default-address
  [db]
  (-> (get db :profile/wallet-accounts)
      get-default-account
      :address))

(defn addresses-without-watch
  [db]
  (into #{}
        (remove #(= (:type %) :watch)
                (map #(eip55/address->checksum (:address %)) (get db :profile/wallet-accounts)))))

(defn naked-address
  [s]
  (when s
    (string/replace s hex-prefix "")))

(def public-key-length 128)

(defn coordinates
  [public-key]
  (when-let [hex (naked-address public-key)]
    (when (= public-key-length (count (subs hex 2)))
      {:x (normalized-hex (subs hex 2 66))
       :y (normalized-hex (subs hex 66))})))

(defn address?
  [s]
  (when s
    (native-module/address? s)))

(defn network->chain-id
  [network]
  (get-in network [:config :NetworkId]))

(defn network->chain-keyword
  [network]
  (chain-id->chain-keyword (network->chain-id network)))

(defn current-network
  [db]
  (let [networks   (get db :networks/networks)
        network-id (get db :networks/current-network)]
    (get networks network-id)))

(defn binance-chain-id?
  [chain-id]
  (or (= BSC-mainnet-chain-id chain-id)
      (= BSC-testnet-chain-id chain-id)))

(defn binance-chain?
  [db]
  (-> db
      current-network
      network->chain-id
      binance-chain-id?))

(def custom-rpc-node-id-len 45)

(defn custom-rpc-node?
  [{:keys [id]}]
  (= custom-rpc-node-id-len (count id)))

(defn network->network-name
  [network]
  (chain-id->chain-name (network->chain-id network)))

(defn network->chain-name
  [network]
  (-> network
      network->chain-keyword
      name))

(defn get-current-network
  [m]
  (get (:networks/networks m) (:networks/current-network m)))

(defn chain-keyword
  [db]
  (network->chain-keyword (get-current-network db)))

(defn chain-id
  [db]
  (network->chain-id (get-current-network db)))

(defn snt-symbol
  [db]
  (chain-keyword->snt-symbol (chain-keyword db)))

(defn address=
  [address1 address2]
  (and address1
       address2
       (= (string/lower-case (normalized-hex address1))
          (string/lower-case (normalized-hex address2)))))

(defn public-key->address
  [public-key]
  (let [length         (count public-key)
        normalized-key (case length
                         132 (str "0x" (subs public-key 4))
                         130 public-key
                         128 (str "0x" public-key)
                         nil)]
    (when normalized-key
      (subs (sha3 normalized-key) 26))))

(defn hex->text
  "Converts a hexstring to UTF8 text."
  [data]
  (or (hex-to-utf8 data) data))
