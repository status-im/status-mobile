(ns status-im.utils.ethereum.core
  (:require [clojure.string :as string]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]))

;; IDs standardized in https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md#list-of-chain-ids

(def chains
  {:mainnet {:id 1 :name "Mainnet"}
   :testnet {:id 3 :name "Ropsten"}
   :rinkeby {:id 4 :name "Rinkeby"}})

(defn chain-id->chain-keyword [i]
  (some #(when (= i (:id (val %))) (key %)) chains))

(defn chain-keyword->chain-id [k]
  (get-in chains [k :id]))

(defn testnet? [id]
  (contains? #{(chain-keyword->chain-id :testnet) (chain-keyword->chain-id :rinkeby)} id))

(defn network-with-upstream-rpc? [networks network]
  (get-in networks [network :raw-config :UpstreamConfig :Enabled]))

(def hex-prefix "0x")

(defn normalized-address [address]
  (when address
    (if (string/starts-with? address hex-prefix)
      address
      (str hex-prefix address))))

(defn address? [s]
  (when s
    (.isAddress dependencies/Web3.prototype s)))

(defn network->chain-keyword [network]
  (when network
    (keyword (string/replace network "_rpc" ""))))

(defn sha3 [s]
  (.sha3 dependencies/Web3.prototype (str s)))

(defn hex->boolean [s]
  (= s "0x0"))

(defn boolean->hex [b]
  (if b "0x0" "0x1"))

(defn hex->int [s]
  (js/parseInt s 16))

(defn int->hex [i]
  (.toHex dependencies/Web3.prototype i))

(defn hex->bignumber [s]
  (money/bignumber (if (= s hex-prefix) 0 s)))

(defn zero-pad-64 [s]
  (str (apply str (drop (count s) (repeat 64 "0"))) s))

(defn format-param [param]
  (if (number? param)
    (zero-pad-64 (hex->int param))
    (zero-pad-64 (subs param 2))))

(defn format-call-params [method-id & params]
  (let [params (string/join (map format-param params))]
    (str method-id params)))

(defn- sig->method-id [signature]
  (apply str (take 10 (sha3 signature))))

(defn call [web3 params cb]
  (.call (.-eth web3) (clj->js params) cb))

(defn call-params [contract method-sig & params]
  (let [data (apply format-call-params (sig->method-id method-sig) params)]
    {:to contract :data data}))

(defn send-transaction [web3 params cb]
  (.sendTransaction (.-eth web3) (clj->js params) cb))

(def default-transaction-gas (money/bignumber 21000))

(defn gas-price [web3 cb]
  (.getGasPrice (.-eth web3) cb))

(defn estimate-gas [symbol]
  (if (tokens/ethereum? symbol)
    default-transaction-gas
    ;; TODO(jeluard) Rely on estimateGas call
    (.times default-transaction-gas 5)))
