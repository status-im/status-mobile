(ns utils.red-black-tree
  (:refer-clojure :exclude [remove update find])
  (:require ["functional-red-black-tree" :as red-black-tree]))

(def tree ^js red-black-tree)

(defn find
  [^js tree item]
  (.find tree item))

(defn insert
  [^js tree item]
  (.insert tree item))

(defn update
  [^js iterator item]
  (.update iterator item))

(defn remove
  [^js iterator]
  (.remove iterator))

(defn get-values
  [^js tree]
  (.-values ^js tree))

(defn get-prev-element
  "Get previous item in the iterator, and wind it back to the initial state"
  [^js iterator]
  (.prev iterator)
  (let [e (.-value iterator)]
    (.next iterator)
    e))

(defn get-prev
  [^js iterator]
  (when (.-hasPrev iterator)
    (get-prev-element iterator)))

(defn get-next-element
  "Get next item in the iterator, and wind it back to the initial state"
  [^js iterator]
  (.next iterator)
  (let [e (.-value iterator)]
    (.prev iterator)
    e))

(defn get-next
  [^js iterator]
  (when (.-hasNext iterator)
    (get-next-element iterator)))
