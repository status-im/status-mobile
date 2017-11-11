(ns status-im.utils.money
  (:require [clojure.string :as string]
            [status-im.js-dependencies :as dependencies]))

;; The BigNumber version included in web3 sometimes hangs when dividing large
;; numbers Hence we want to use these functions instead of fromWei etc, which
;; come bundled with web3. See
;; https://github.com/MikeMcl/bignumber.js/issues/120 for this regression being
;; introduced in some JS environments. It is fixed in the MikeMcl/bignumber.js
;; repo, but not in the web3 BigNumber fork:
;; https://github.com/ethereum/web3.js/issues/877
;;
;; Additionally, while it is possible to use the BigNumber constructor without
;; stringifying the number, this only works up to some 15 significant digits:
;; https://github.com/MikeMcl/bignumber.js/issues/120
;;
;; Lastly, notice the bad rounding for native Javascript numbers above 17 digits
;; that may result in errors earlier up the call chain. Ideally all money-related
;; sensitive functions should be moved into this namespace to check for such
;; matters:
;; (str 111122223333441239) => "111122223333441230"

(defn normalize
  "A normalized string representation of an amount"
  [s]
  {:pre [(or (nil? s) (string? s))]}
  (when s
    (string/replace (string/trim s) #"," ".")))

(defn bignumber [n]
  (when n
    (try
      (dependencies/Web3.prototype.toBigNumber (str n))
      (catch :default err nil))))

(defn valid? [bn]
  (when bn
    (.greaterThanOrEqualTo bn 0)))

(defn str->wei [s]
  (when-let [ns (normalize s)]
    (try
      (dependencies/Web3.prototype.toWei ns "ether")
      (catch :default err nil))))

(defn to-decimal [s]
  (when s
    (try
      (dependencies/Web3.prototype.toDecimal (normalize s))
      (catch :default err nil))))

(def eth-units
  {:wei    (bignumber "1")
   :kwei   (bignumber "1000")
   :mwei   (bignumber "1000000")
   :gwei   (bignumber "1000000000")
   :szabo  (bignumber "1000000000000")
   :finney (bignumber "1000000000000000")
   :eth    (bignumber "1000000000000000000")
   :keth   (bignumber "1000000000000000000000")
   :meth   (bignumber "1000000000000000000000000")
   :geth   (bignumber "1000000000000000000000000000")
   :teth   (bignumber "1000000000000000000000000000000")})

(defn wei-> [unit n]
  (when-let [bn (bignumber n)]
    (.dividedBy bn (eth-units unit))))

(defn to-fixed [bn]
  (when bn
    (.toFixed bn)))

(defn wei->str [unit n]
  (str (to-fixed (wei-> unit n)) " " (string/upper-case (name unit))))

(defn wei->ether [n]
  (wei-> :eth n))

(defn ether->wei [bn]
  (when bn
    (.times bn (bignumber 1e18))))

(defn fee-value [gas gas-price]
  (.times (bignumber gas) (bignumber gas-price)))

(defn eth->usd [eth usd-price]
  (.times (bignumber eth) (bignumber usd-price)))

(defn percent-change [from to]
  (let [bnf (bignumber from)
        bnt (bignumber to)]
    (when (and bnf bnt)
      (-> (.dividedBy bnf bnt)
          (.minus 1)
          (.times 100)))))

(defn with-precision [n decimals]
  (when-let [bn (bignumber n)]
    (.round bn decimals)))

(defn sufficient-funds? [amount balance]
  (when amount
    (.greaterThanOrEqualTo balance amount)))
