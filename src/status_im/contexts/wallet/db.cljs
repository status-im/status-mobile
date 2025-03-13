(ns status-im.contexts.wallet.db
  (:require [clojure.core :as core]
            [status-im.constants :as constants])
  (:refer-clojure :exclude [assoc-in get-in update-in]))

(def swap [:wallet :ui :swap])

(defn- flatten-path
  "Receives a vector of vals and flattens them
  (flatten-path [:a [:b :c]]) returns [:a :b :c]
  (flatten-path [[:a] [:b :c]]) returns [:a :b :c]"
  [ks]
  (loop [elements ks
         result   []]
    (let [v (first elements)]
      (if-not v
        result
        (if (sequential? v)
          (recur (rest elements) (into result v))
          (recur (rest elements) (conj result v)))))))

(defn assoc-in
  "Version of assoc-in that can receive vectors as parts of the path.
  Example:
  (def swap [:wallet :ui :swap])
  (assoc-in db [swap :some-key] \"value\")"
  [m ks v]
  (core/assoc-in m (flatten-path ks) v))

(defn update-in
  "Version of update-in that can receive vectors as parts of the path.
  Example:
  (def swap [:wallet :ui :swap])
  (update-in db [swap :some-key] \"value\")"
  [m ks & args]
  (apply core/update-in m (flatten-path ks) args))

(defn get-in
  "Version of get-in that can receive vectors as parts of the path.
  Example:
  (def swap [:wallet :ui :swap])
  (update-in db [swap :some-key] \"value\")"
  ([m ks not-found]
   (core/get-in m (flatten-path ks) not-found))
  ([m ks]
   (core/get-in m (flatten-path ks) nil)))

(def network-filter-defaults
  {:selector-state    :default
   :selected-networks (set constants/default-network-names)})

(def defaults
  {:ui {:network-filter network-filter-defaults
        ;; Note: we set it to nil by default to differentiate when the user logs
        ;; in and the device is offline, versus re-fetching when offline and
        ;; tokens already exist in the app-db.
        :tokens-loading nil
        :active-tab     :assets}})
