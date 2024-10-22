(ns status-im.contexts.wallet.send.input-amount.controlled-input-logic
  (:require
    [clojure.string :as string]
    [status-im.contexts.wallet.common.utils :as wallet-utils]
    [utils.money :as money]))

(def ^:private default-max-limit 12)

(def ^:private not-digits-or-dot-pattern
  #"[^0-9+\.]")

(def ^:private dot ".")

(defn- can-add-character?
  [input-value character max-length]
  (let [length-overflow?    (>= (count input-value) max-length)
        extra-dot?          (and (= character dot) (string/includes? input-value dot))
        extra-leading-zero? (and (= input-value "0") (= "0" (str character)))
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
  ([input-value character]
   (add-character input-value character default-max-limit))
  ([input-value character max-length]
   (if (can-add-character? input-value character max-length)
     (normalize-value-as-numeric input-value character)
     input-value)))

(defn delete-last
  ([input-value]
   (delete-last input-value ""))
  ([input-value default-value]
   (let [new-value (subs input-value 0 (dec (count input-value)))]
     (if (string/blank? new-value)
       default-value
       new-value))))

(defn upper-limit-exceeded?
  [input-value upper-limit]
  (and upper-limit
       (when (money/bignumber? (money/bignumber input-value))
         (money/greater-than (money/bignumber input-value) (money/bignumber upper-limit)))))

(defn lower-limit-exceeded?
  [input-value lower-limit]
  (and lower-limit
       (when (money/bignumber? (money/bignumber input-value))
         (money/less-than (money/bignumber input-value) (money/bignumber lower-limit)))))

(defn value-out-of-limits?
  [input-value upper-limit lower-limit]
  (or (upper-limit-exceeded? input-value upper-limit)
      (lower-limit-exceeded? input-value lower-limit)))

(defn value-numeric
  [input-value]
  (or (parse-double input-value) 0))

(defn empty-value?
  [input-value]
  (or (string/blank? input-value) (<= (value-numeric input-value) 0)))
