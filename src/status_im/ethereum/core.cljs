(ns status-im.ethereum.core
  (:require [clojure.string :as string]
            [status-im.ethereum.tokens :as tokens]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.money :as money]))

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

(defn naked-address [s]
  (when s
    (string/replace s hex-prefix "")))

(def ^:const public-key-length 128)

(defn coordinates [public-key]
  (when-let [hex (naked-address public-key)]
    (when (= public-key-length (count (subs hex 2)))
      {:x (normalized-address (subs hex 1 65))
       :y (normalized-address (subs hex 66))})))

(defn address? [s]
  (when s
    (.isAddress (dependencies/web3-prototype) s)))

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
   (.sha3 (dependencies/web3-prototype) (str s)))
  ([s opts]
   (.sha3 (dependencies/web3-prototype) (str s) (clj->js opts))))

(defn hex->string [s]
  (when s
    (let [hex (.toString s)]
      (loop [res "" i (if (string/starts-with? hex hex-prefix) 2 0)]
        (if (and (< i (.-length hex)))
          (recur
           (if (= (.substr hex i 2) "00")
             res
             (str res (.fromCharCode js/String (js/parseInt (.substr hex i 2) 16))))
           (+ i 2))
          res)))))

(defn hex->boolean [s]
  (= s "0x0"))

(defn boolean->hex [b]
  (if b "0x0" "0x1"))

(defn hex->int [s]
  (if (= s hex-prefix)
    0
    (js/parseInt s 16)))

(defn int->hex [i]
  (.toHex dependencies/Web3.prototype i))

(defn hex->bignumber [s]
  (money/bignumber (if (= s hex-prefix) 0 s)))

(def ^:const public-key-length 128)

(defn hex->address
  "When hex value is 66 char in length (2 for 0x, 64 for
  the 32 bytes used by abi-spec for an address), only keep
  the part that constitute the address and normalize it,"
  [s]
  (when (= 66 (count s))
    (normalized-address (subs s 26))))

(defn coordinates [public-key]
  (when-let [hex (naked-address public-key)]
    (when (= public-key-length (count (subs hex 2)))
      {:x (normalized-address (subs hex 2 65))
       :y (normalized-address (subs hex 66))})))

(defn zero-pad-64 [s]
  (str (apply str (drop (count s) (repeat 64 "0"))) s))

(defn string->hex [i]
  (.fromAscii dependencies/Web3.prototype i))

(defn format-param [param]
  (if (number? param)
    (zero-pad-64 (str (hex->int param)))
    (zero-pad-64 (subs param 2))))

(defn format-call-params [method-id & params]
  (let [params (string/join (map format-param params))]
    (str method-id params)))

(defn- sig->method-id [signature]
  (apply str (take 10 (sha3 signature))))

(defn call [params callback]
  (json-rpc/call
   {:method "eth_call"
    :params [params "latest"]
    :on-success callback}))

(defn call-params [contract method-sig & params]
  (let [data (apply format-call-params (sig->method-id method-sig) params)]
    {:to contract :data data}))

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
