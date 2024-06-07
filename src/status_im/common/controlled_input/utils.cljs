(ns status-im.common.controlled-input.utils
  (:require
    [clojure.string :as string]))

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
  (or (parse-double (input-value state)) 0))

(defn input-error
  [state]
  (:error? state))

(defn- set-input-error
  [state error?]
  (assoc state :error? error?))

(defn upper-limit
  [state]
  (:upper-limit state))

(defn lower-limit
  [state]
  (:lower-limit state))

(defn upper-limit-exceeded?
  [state]
  (and
   (upper-limit state)
   (> (numeric-value state) (upper-limit state))))

(defn lower-limit-exceeded?
  [state]
  (and
   (lower-limit state)
   (< (numeric-value state) (lower-limit state))))

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
  (let [new-val (inc (numeric-value state))]
    (set-input-value state (str new-val))))

(defn decrease
  [state]
  (let [new-val (dec (numeric-value state))]
    (set-input-value state (str new-val))))

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

