(ns status-im.ethereum.core
  (:require [clojure.string :as string]
            [status-im.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]
            ["web3-utils" :as utils]))

(defn sha3 [s]
  (when s
    (.sha3 utils (str s))))

(defn utf8-to-hex [s]
  (try
    (.utf8ToHex utils (str s))
    (catch :default _ nil)))

(defn hex-to-utf8 [s]
  (try
    (.hexToUtf8 utils s)
    (catch :default _ nil)))

;; IDs standardized in https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md#list-of-chain-ids

(def chains
  {:mainnet {:id 1 :name "Mainnet"}
   :testnet {:id 3 :name "Ropsten"}
   :rinkeby {:id 4 :name "Rinkeby"}
   :xdai    {:id 100 :name "xDai"}
   :poa     {:id 99 :name "POA"}
   :goerli  {:id 5 :name "Goerli"}})

(defn chain-id->chain-keyword [i]
  (or (some #(when (= i (:id (val %))) (key %)) chains)
      :custom))

(defn chain-id->chain-name [i]
  (or (some #(when (= i (:id (val %))) (:name (val %))) chains)
      :custom))

(defn chain-keyword->chain-id [k]
  (get-in chains [k :id]))

(defn testnet? [id]
  (contains? #{(chain-keyword->chain-id :testnet)
               (chain-keyword->chain-id :rinkeby)
               (chain-keyword->chain-id :goerli)} id))

(defn sidechain? [id]
  (contains? #{(chain-keyword->chain-id :xdai)
               (chain-keyword->chain-id :poa)} id))

(defn network-with-upstream-rpc? [network]
  (get-in network [:config :UpstreamConfig :Enabled]))

(def hex-prefix "0x")

(defn normalized-hex [hex]
  (when hex
    (if (string/starts-with? hex hex-prefix)
      hex
      (str hex-prefix hex))))

(defn current-address [db]
  (-> (get-in db [:multiaccount :address])
      normalized-hex))

(defn get-default-account [accounts]
  (some #(when (:wallet %) %) accounts))

(defn default-address [db]
  (-> (get db :multiaccount/accounts)
      get-default-account
      :address))

(defn naked-address [s]
  (when s
    (string/replace s hex-prefix "")))

(def public-key-length 128)

(defn coordinates [public-key]
  (when-let [hex (naked-address public-key)]
    (when (= public-key-length (count (subs hex 2)))
      {:x (normalized-hex (subs hex 2 66))
       :y (normalized-hex (subs hex 66))})))

(defn address? [s]
  (when s
    (.isAddress utils s)))

(defn network->chain-id [network]
  (get-in network [:config :NetworkId]))

(defn network->chain-keyword [network]
  (chain-id->chain-keyword (network->chain-id network)))

(defn network->network-name [network]
  (chain-id->chain-name (network->chain-id network)))

(defn network->chain-name [network]
  (-> network
      network->chain-keyword
      name))

(defn chain-keyword
  [{:networks/keys [current-network networks]}]
  (network->chain-keyword (get networks current-network)))

(defn chain-id
  [{:networks/keys [current-network networks]}]
  (network->chain-id (get networks current-network)))

(defn snt-symbol [db]
  (case (chain-keyword db)
    :mainnet :SNT
    :STT))

(def default-transaction-gas (money/bignumber 21000))

(defn estimate-gas [symbol]
  (if (tokens/ethereum? symbol)
    default-transaction-gas
    ;; TODO(jeluard) Rely on estimateGas call
    (.times ^js default-transaction-gas 5)))

(defn address= [address1 address2]
  (and address1 address2
       (= (normalized-hex address1)
          (normalized-hex address2))))

(defn public-key->address [public-key]
  (let [length (count public-key)
        normalized-key (case length
                         132 (str "0x" (subs public-key 4))
                         130 public-key
                         128 (str "0x" public-key)
                         nil)]
    (when normalized-key
      (subs (sha3 normalized-key) 26))))

(def bytes32-length 66) ; length of '0x' + 64 hex values. (a 32bytes value has 64 nibbles)

(defn hex->text
  "Converts a hexstring to UTF8 text. If the data received is 32 bytes long,
   return the value unconverted"
  [data]
  (if (= bytes32-length (count (normalized-hex data)))
    data ; Assume it's a bytes32
    (hex-to-utf8 data)))
