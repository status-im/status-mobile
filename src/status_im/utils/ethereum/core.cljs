(ns status-im.utils.ethereum.core
  (:require [clojure.string :as string]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]))

;; IDs standardized in https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md#list-of-chain-ids

(def chains
  {:mainnet {:id 1 :name "Mainnet"}
   :testnet {:id 3 :name "Ropsten"}
   :rinkeby {:id 4 :name "Rinkeby"}
   :xdai    {:id 100 :name "xDai"}
   :poa     {:id 99 :name "POA"}})

(defn chain-id->chain-keyword [i]
  (or (some #(when (= i (:id (val %))) (key %)) chains)
      :custom))

(defn chain-keyword->chain-id [k]
  (get-in chains [k :id]))

(defn testnet? [id]
  (contains? #{(chain-keyword->chain-id :testnet)
               (chain-keyword->chain-id :rinkeby)} id))

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
  (-> (get-in db [:account/account :address])
      normalized-address))

(defn naked-address [s]
  (when s
    (string/replace s hex-prefix "")))

(defn address? [s]
  (when s
    (.isAddress dependencies/Web3.prototype s)))

(defn network->chain-id [network]
  (get-in network [:config :NetworkId]))

(defn network->chain-keyword [network]
  (chain-id->chain-keyword (network->chain-id network)))

(defn network->chain-name [network]
  (-> network
      network->chain-keyword
      name))

(defn chain-keyword
  [db]
  (let [network-id (get-in db [:account/account :network])
        network    (get-in db [:account/account :networks network-id])]
    (network->chain-keyword network)))

(defn sha3
  ([s]
   (.sha3 dependencies/Web3.prototype (str s)))
  ([s opts]
   (.sha3 dependencies/Web3.prototype (str s) (clj->js opts))))

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
