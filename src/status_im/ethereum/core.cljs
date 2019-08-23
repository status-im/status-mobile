(ns status-im.ethereum.core
  (:require [clojure.string :as string]
            [status-im.ethereum.tokens :as tokens]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.money :as money]))

(defn utils [] (dependencies/web3-utils))

(defn sha3 [s]
  (when s
    (.sha3 (utils) (str s))))

(defn utf8-to-hex [s]
  (try
    (.utf8ToHex (utils) (str s))
    (catch :default err nil)))

(defn hex-to-utf8 [s]
  (try
    (.hexToUtf8 (utils) s)
    (catch :default err nil)))

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

(defn normalized-address [address]
  (when address
    (if (string/starts-with? address hex-prefix)
      address
      (str hex-prefix address))))

(defn current-address [db]
  (-> (get-in db [:multiaccount :address])
      normalized-address))

(defn get-default-account [accounts]
  (some #(when (:wallet %) %) accounts))

(defn default-address [db]
  (-> (get-in db [:multiaccount :accounts])
      get-default-account
      :address))

(defn naked-address [s]
  (when s
    (string/replace s hex-prefix "")))

(def ^:const public-key-length 128)

(defn coordinates [public-key]
  (when-let [hex (naked-address public-key)]
    (when (= public-key-length (count (subs hex 2)))
      {:x (normalized-address (subs hex 2 66))
       :y (normalized-address (subs hex 66))})))

(defn address? [s]
  (when s
    (.isAddress (utils) s)))

(defn network->chain-id [network]
  (get-in network [:config :NetworkId]))

(defn network->chain-keyword [network]
  (chain-id->chain-keyword (network->chain-id network)))

(defn network->chain-name [network]
  (-> network
      network->chain-keyword
      name))

(defn chain-keyword
  [{:networks/keys [current-network networks]}]
  (network->chain-keyword (get networks current-network)))

(defn snt-symbol [db]
  (case (chain-keyword db)
    :mainnet :SNT
    :STT))

(def default-transaction-gas (money/bignumber 21000))

(defn estimate-gas [symbol]
  (if (tokens/ethereum? symbol)
    default-transaction-gas
    ;; TODO(jeluard) Rely on estimateGas call
    (.times default-transaction-gas 5)))

(defn address= [address1 address2]
  (and address1 address2
       (= (normalized-address address1)
          (normalized-address address2))))

(defn public-key->address [public-key]
  (let [length (count public-key)
        normalized-key (case length
                         132 (str "0x" (subs public-key 4))
                         130 public-key
                         128 (str "0x" public-key)
                         nil)]
    (when normalized-key
      (subs (sha3 normalized-key) 26))))
