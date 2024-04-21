(ns status-im.common.controlled-input.utils
  (:require
    [clojure.string :as string]
    [reagent.core :as reagent]))

(defn create-input-state
  []
  (reagent/atom {:value       ""
                 :error?      false
                 :upper-limit nil}))

(defn input-value
  [state]
  (:value @state))

(defn numeric-value
  [state]
  (or (parse-double (input-value state)) 0))

(defn input-error
  [state]
  (:error? @state))

(defn- set-input-error
  [state error?]
  (swap! state assoc :error? error?))

(defn- upper-limit
  [state]
  (:upper-limit @state))

(defn upper-limit-exceeded?
  [state]
  (and
   (upper-limit state)
   (> (numeric-value state) (upper-limit state))))

(defn- recheck-errorness
  [state]
  (set-input-error state (upper-limit-exceeded? state)))


(defn- set-input-value
  [state value]
  (swap! state assoc :value value)
  (recheck-errorness state))


(defn set-upper-limit
  [state limit]
  (swap! state assoc :upper-limit limit)
  (recheck-errorness state))


(def not-digits-or-dot-pattern
  #"[^0-9+\.]")

(def dot ".")

(defn- can-add-character?
  [state c]
  (let [max-length          12
        current             (input-value state)
        length-overflow?    (>= (count current) max-length)
        extra-dot?          (and (= c dot) (string/includes? current dot))
        extra-leading-zero? (and (= current "0") (= "0" (str c)))
        non-numeric?        (re-find not-digits-or-dot-pattern (str c))]
    (not (or non-numeric? extra-dot? extra-leading-zero? length-overflow?))))


(defn- normalize-value-as-numeric
  [value c]
  (cond
    (and (string/blank? value) (= c dot))
    (str "0" c)

    (and (= value "0") (not= c dot))
    (str c)

    :else
    (str value c)))

(defn add-character
  [state c]
  (when (can-add-character? state c)
    (set-input-value state
                     (normalize-value-as-numeric (input-value state) c))))

(defn delete-last
  [state]
  (let [value     (input-value state)
        new-value (subs value 0 (dec (count value)))]
    (set-input-value state new-value)))

(defn delete-all
  [state]
  (set-input-value state ""))

