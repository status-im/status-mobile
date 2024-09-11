(ns status-im.common.controlled-input.utils
  (:require
    [clojure.string :as string]
    [status-im.contexts.wallet.common.utils :as wallet-utils]
    [utils.money :as money]))

(def init-state
  {:value       ""
   :error?      false
   :upper-limit nil
   :lower-limit nil})

(defn input-value
  [state]
  (:value state))

(defn numeric-value
  [state]
  (money/bignumber (input-value state)))

(defn input-error
  [state]
  (:error? state))

(defn- set-input-error
  [state error?]
  (assoc state :error? error?))

(defn upper-limit
  [state]
  (money/bignumber (:upper-limit state)))

(defn lower-limit
  [state]
  (money/bignumber (:lower-limit state)))

(defn upper-limit-exceeded?
  [state]
  (let [num-value (numeric-value state)]
    (and
     (upper-limit state)
     (when (money/bignumber? num-value)
       (money/greater-than (numeric-value state) (upper-limit state))))))

(defn- lower-limit-exceeded?
  [state]
  (let [num-value (numeric-value state)]
    (and
     (lower-limit state)
     (when (money/bignumber? num-value)
       (money/less-than (numeric-value state) (lower-limit state))))))

(defn- recheck-errorness
  [state]
  (set-input-error state
                   (or (upper-limit-exceeded? state)
                       (lower-limit-exceeded? state))))

(defn set-input-value
  [state value]
  (-> state
      (assoc :value value)
      recheck-errorness))

(defn set-numeric-value
  [state value]
  (set-input-value state (str value)))

(defn set-upper-limit
  [state limit]
  (when limit
    (-> state
        (assoc :upper-limit limit)
        recheck-errorness)))

(defn set-lower-limit
  [state limit]
  (-> state
      (assoc :lower-limit limit)
      recheck-errorness))

(defn increase
  [state]
  (set-input-value state (str (money/add (numeric-value state) 1))))

(defn decrease
  [state]
  (set-input-value state (str (money/add (numeric-value state) -1))))

(def ^:private not-digits-or-dot-pattern
  #"[^0-9+\.]")

(def ^:private dot ".")

(defn- can-add-character?
  [state character]
  (let [max-length          12
        current             (input-value state)
        length-overflow?    (>= (count current) max-length)
        extra-dot?          (and (= character dot) (string/includes? current dot))
        extra-leading-zero? (and (= current "0") (= "0" (str character)))
        non-numeric?        (re-find not-digits-or-dot-pattern (str character))]
    (not (or non-numeric? extra-dot? extra-leading-zero? length-overflow?))))

(defn- normalize-value-as-numeric
  [value character]
  (cond
    (and (string/blank? value) (= character dot))
    (str "0" character)

    (and (= value "0") (not= character dot))
    (str character)

    :else
    (str value character)))

(defn add-character
  [state character]
  (if (can-add-character? state character)
    (set-input-value state
                     (normalize-value-as-numeric (input-value state) character))
    state))

(defn delete-last
  [state]
  (let [value     (input-value state)
        new-value (subs value 0 (dec (count value)))]
    (set-input-value state new-value)))

(defn delete-all
  [state]
  (set-input-value state ""))

(defn empty-value?
  [state]
  (string/blank? (:value state)))

(defn- fiat->crypto
  [value conversion-rate]
  (-> value
      (money/fiat->crypto conversion-rate)
      (wallet-utils/cut-crypto-decimals-to-fit-usd-cents conversion-rate)))

(defn- crypto->fiat
  [value conversion-rate]
  (-> value
      (money/crypto->fiat conversion-rate)
      (wallet-utils/cut-fiat-balance-to-two-decimals)))

(defn ->crypto
  [state conversion-rate]
  (-> state
      (set-input-value (fiat->crypto (input-value state) conversion-rate))
      (set-upper-limit (fiat->crypto (upper-limit state) conversion-rate))
      (set-lower-limit (fiat->crypto (lower-limit state) conversion-rate))))

(defn ->fiat
  [state conversion-rate]
  (-> state
      (set-input-value (crypto->fiat (input-value state) conversion-rate))
      (set-upper-limit (crypto->fiat (upper-limit state) conversion-rate))
      (set-lower-limit (crypto->fiat (lower-limit state) conversion-rate))))
