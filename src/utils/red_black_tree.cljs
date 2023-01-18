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
  [^js iter item]
  (.update iter item))

(defn remove
  [^js iter]
  (.remove iter))

(defn get-values
  [^js tree]
  (.-values ^js tree))

(defn get-prev-element
  "Get previous item in the iterator, and wind it back to the initial state"
  [^js iter]
  (.prev iter)
  (let [e (.-value iter)]
    (.next iter)
    e))

(defn get-prev
  [^js iter]
  (when (.-hasPrev iter)
    (get-prev-element iter)))

(defn get-next-element
  "Get next item in the iterator, and wind it back to the initial state"
  [^js iter]
  (.next iter)
  (let [e (.-value iter)]
    (.prev iter)
    e))

(defn get-next
  [^js iter]
  (when (.-hasNext iter)
    (get-next-element iter)))
