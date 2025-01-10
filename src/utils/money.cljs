(ns utils.money
  (:require
    ["bignumber.js" :as BigNumber]
    [clojure.string :as string]
    [schema.core :as schema]
    [utils.i18n :as i18n]))

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

(defn bignumber
  [n]
  (when n
    (try
      (new BigNumber (normalize (str n)))
      (catch :default _ nil))))

(defn bignumber?
  "Check if the value is a bignumber."
  [x]
  (instance? BigNumber x))

(defn ->bignumber
  [n]
  (if (bignumber? n) n (bignumber n)))

(defn ->bignumbers
  [& numbers]
  (let [transformed-numbers (map ->bignumber numbers)]
    (when (every? bignumber? transformed-numbers)
      transformed-numbers)))

(defn greater-than-or-equals
  [^js n1 ^js n2]
  (when-let [[^js bn1 ^js bn2] (->bignumbers n1 n2)]
    (.greaterThanOrEqualTo bn1 bn2)))

(defn greater-than
  [n1 n2]
  (when-let [[^js bn1 ^js bn2] (->bignumbers n1 n2)]
    (.greaterThan ^js bn1 bn2)))

(defn less-than
  [n1 n2]
  (when-let [[^js bn1 ^js bn2] (->bignumbers n1 n2)]
    (.lessThan ^js bn1 bn2)))

(defn equal-to
  [n1 n2]
  (when-let [[^js bn1 ^js bn2] (->bignumbers n1 n2)]
    (.eq ^js bn1 bn2)))

(extend-type BigNumber
 IEquiv
   (-equiv [this other]
     (if (or (number? other)
             (string? other)
             (instance? BigNumber other))
       (equal-to this other)
       false))

 IComparable
   (-compare [this other]
     (cond
       (less-than this other)    -1
       (greater-than this other) 1
       :else                     0)))

(defn sub
  [n1 n2]
  (when-let [[^js bn1 ^js bn2] (->bignumbers n1 n2)]
    (.sub ^js bn1 bn2)))

(defn valid?
  [^js bn]
  (when bn
    (greater-than-or-equals bn 0)))

(defn from-decimal
  [n]
  (when n
    (str "1" (string/join (repeat n "0")))))

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

(defn wei->
  [unit n]
  (when-let [^js bn (bignumber n)]
    (.dividedBy bn (eth-units unit))))

(defn ->wei
  [unit n]
  (when-let [^js bn (bignumber n)]
    (.times bn (eth-units unit))))

(defn to-fixed
  ([^js bn]
   (when bn
     (.toFixed bn)))
  ([^js bn b]
   (when bn
     (.toFixed bn b))))

(defn to-string
  ([^js bn]
   (to-string bn 10))
  ([^js bn base]
   (when bn
     (.toString bn base))))

(defn to-hex
  [^js bn]
  (str "0x" (to-string bn 16)))

(defn from-hex
  [hex-str]
  (try
    (new BigNumber hex-str 16)
    (catch :default _ nil)))

(defn wei->ether
  [n]
  (wei-> :eth n))

(defn wei->gwei
  [n]
  (wei-> :gwei n))

(defn gwei->wei
  [n]
  (->wei :gwei n))

(defn token->unit
  [n decimals]
  (when-let [^js bn (bignumber n)]
    (when-let [d (from-decimal decimals)]
      (.dividedBy bn ^js (bignumber d)))))

(defn with-precision
  [n decimals]
  (when-let [^js bn (bignumber n)]
    (.round bn decimals)))

(defn crypto->fiat
  [crypto fiat-price]
  (let [^js crypto-bn     (bignumber crypto)
        ^js fiat-price-bn (bignumber fiat-price)]
    (when (and crypto-bn fiat-price-bn)
      (-> crypto-bn
          (.times fiat-price-bn)))))

(defn above-zero?
  [^js balance]
  (when balance
    (->> 0
         bignumber
         (greater-than balance))))

(defn sufficient-funds?
  [^js amount ^js balance]
  (when (and amount balance)
    (greater-than-or-equals balance amount)))

(defn- add*
  [bn1 n2]
  (.add ^js bn1 n2))

(def add
  "Add with defaults, this version is able to receive `nil` and takes them as 0."
  (fnil add* (bignumber 0) (bignumber 0)))

(defn- mul*
  [bn1 bn2]
  (.mul ^js bn1 bn2))

(def mul
  "Multiply with defaults, this version is able to receive `nil` and takes them as 0."
  (fnil mul* (bignumber 0) (bignumber 0)))

(defn- div*
  [bn1 bn2]
  (.dividedBy ^js bn1 bn2))

(def div
  "Divides with defaults, this version is able to receive `nil` and takes them as 0."
  (fnil div* (bignumber 0) (bignumber 1)))

(defn fiat->crypto
  [crypto fiat-price]
  (when-let [crypto-bn (bignumber crypto)]
    (div crypto-bn
         (bignumber fiat-price))))

(defn absolute-value
  [bn]
  (when (bignumber? bn)
    (.absoluteValue ^js bn)))

(defn format-amount
  "Format `amount` to thousands or millions. Return nil if `amount` is not truthy."
  [amount]
  (when amount
    (cond
      (> amount 999999)
      (str (with-precision (/ amount 1000000) 1) (i18n/label :t/M))

      (< 999 amount 1000000)
      (str (with-precision (/ amount 1000) 1) (i18n/label :t/K))

      :else
      (str amount))))

(schema/=> format-amount
  [:=> [:cat [:maybe :int]]
   [:maybe :string]])
