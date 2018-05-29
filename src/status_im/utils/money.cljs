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
      (dependencies/Web3.prototype.toBigNumber (normalize (str n)))
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

(defn from-decimal [n] (when n (str "1" (string/join (repeat n "0")))))

(def eth-units
  {:wei    (bignumber "1")
   :kwei   (bignumber (from-decimal 3))
   :mwei   (bignumber (from-decimal 6))
   :gwei   (bignumber (from-decimal 9))
   :szabo  (bignumber (from-decimal 12))
   :finney (bignumber (from-decimal 15))
   :eth    (bignumber (from-decimal 18))
   :keth   (bignumber (from-decimal 21))
   :meth   (bignumber (from-decimal 24))
   :geth   (bignumber (from-decimal 27))
   :teth   (bignumber (from-decimal 30))})

(defn wei-> [unit n]
  (when-let [bn (bignumber n)]
    (.dividedBy bn (eth-units unit))))

(defn ->wei [unit n]
  (when-let [bn (bignumber n)]
    (.times bn (eth-units unit))))

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

(defn token->unit [n decimals]
  (when-let [bn (bignumber n)]
    (.dividedBy bn (bignumber (from-decimal decimals)))))

(defn unit->token [n decimals]
  (when-let [bn (bignumber n)]
    (.times bn (bignumber (from-decimal decimals)))))

;;NOTE(goranjovic) - We have two basic representations of values that refer to cryptocurrency amounts: formatted and
;; internal. Formatted representation is the one we show on screens and include in reports, whereas internal
;; representation is the one that we pass on to ethereum network for execution, transfer, etc.
;; The difference between the two depends on the number of decimals, i.e. internal representation is expressed in terms
;; of a whole number of smallest divisible parts of the formatted value.
;;
;; E.g. for Ether, it's smallest part is wei or 10^(-18) of 1 ether
;; for arbitrary ERC20 token the smallest part is 10^(-decimals) of 1 token
;;
;; Different tokens can have different number of allowed decimals, so it's neccessary to include the decimals parameter
;; to get the amount scale right.

(defn formatted->internal [n symbol decimals]
  (if (= :ETH symbol)
    (ether->wei n)
    (unit->token n decimals)))

(defn internal->formatted [n symbol decimals]
  (if (= :ETH symbol)
    (wei->ether n)
    (token->unit n decimals)))

(defn fee-value [gas gas-price]
  (.times (bignumber gas) (bignumber gas-price)))

(defn eth->fiat [eth fiat-price]
  (when-let [bn (bignumber eth)]
    (.times bn (bignumber fiat-price))))

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
  (when (and amount balance)
    (.greaterThanOrEqualTo balance amount)))
