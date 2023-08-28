(ns status-im.utils.priority-map
  (:require [cljs.core :as core]
            [cljs.reader :refer [register-tag-parser!]])
  (:require-macros [cljs.core :as coreclj]))

;; from
;; https://github.com/tailrecursion/cljs-priority-map/blob/master/src/cljs/tailrecursion/priority_map.cljs
;; fixing `vals` and `keys` function

#_{:clj-kondo/ignore [:shadowed-var]}
(deftype PersistentPriorityMap [priority->set-of-items item->priority meta keyfn ^:mutable __hash]
  IPrintWithWriter
    (-pr-writer [coll writer opts]
      (let [pr-pair (fn [keyval]
                      (pr-sequential-writer writer #'cljs.core/pr-writer "" " " "" opts keyval))]
        (pr-sequential-writer writer pr-pair "#status-im.utils.priority-map {" ", " "}" opts coll)))

  IWithMeta
    (-with-meta [_this meta]
      (PersistentPriorityMap. priority->set-of-items item->priority meta keyfn __hash))

  IMeta
    (-meta [_this] meta)

  ICollection
    (-conj [this entry]
      (if (vector? entry)
        (-assoc this (-nth entry 0) (-nth entry 1))
        (reduce -conj this entry)))

  IEmptyableCollection
    (-empty [_this]
      #_{:clj-kondo/ignore [:unresolved-symbol]}
      (with-meta
        status-im.utils.priority-map.PersistentPriorityMap.EMPTY
        meta))

  IEquiv
    (-equiv [_this other]
      (-equiv item->priority other))

  IHash
    (-hash [this]
      (coreclj/caching-hash this core/hash-unordered-coll __hash))

  ISeqable
    (-seq [_this]
      (if keyfn
        (seq (for [[_ item-set] priority->set-of-items
                   item         item-set]
               (MapEntry. item (item->priority item) nil)))
        (seq (for [[priority item-set] priority->set-of-items
                   item                item-set]
               (MapEntry. item priority nil)))))

  IReversible
    (-rseq [_coll]
      (if keyfn
        (seq (for [[_ item-set] (rseq priority->set-of-items)
                   item         item-set]
               (MapEntry. item (item->priority item) nil)))
        (seq (for [[priority item-set] (rseq priority->set-of-items)
                   item                item-set]
               (MapEntry. item priority nil)))))

  ICounted
    (-count [_this]
      (count item->priority))

  ILookup
    (-lookup [_this item]
      (get item->priority item))
    (-lookup [_coll item not-found]
      (get item->priority item not-found))

  IStack
    (-peek [_this]
      (when-not (zero? (count item->priority))
        (let [f    (first priority->set-of-items)
              item (first (val f))]
          (if keyfn
            [item (item->priority item)]
            [item (key f)]))))
    (-pop [_this]
      (if (zero? (count item->priority))
        (throw (js/Error. "Can't pop empty priority map"))
        (let [f            (first priority->set-of-items)
              item-set     (val f)
              item         (first item-set)
              priority-key (key f)]
          (if (= (count item-set) 1)
            (PersistentPriorityMap.
             (dissoc priority->set-of-items priority-key)
             (dissoc item->priority item)
             meta
             keyfn
             nil)
            (PersistentPriorityMap.
             (assoc priority->set-of-items priority-key (disj item-set item))
             (dissoc item->priority item)
             meta
             keyfn
             nil)))))

  IAssociative
    (-assoc [this item priority]
      (if-let [current-priority (get item->priority item nil)]
        (if (= current-priority priority)
          this
          (let [priority-key         (keyfn priority)
                current-priority-key (keyfn current-priority)
                item-set             (get priority->set-of-items current-priority-key)]
            (if (= (count item-set) 1)
              (PersistentPriorityMap.
               (assoc (dissoc priority->set-of-items current-priority-key)
                      priority-key
                      (conj (get priority->set-of-items priority-key #{}) item))
               (assoc item->priority item priority)
               meta
               keyfn
               nil)
              (PersistentPriorityMap.
               (assoc priority->set-of-items
                      current-priority-key
                      (disj (get priority->set-of-items current-priority-key) item)
                      priority-key
                      (conj (get priority->set-of-items priority-key #{}) item))
               (assoc item->priority item priority)
               meta
               keyfn
               nil))))
        (let [priority-key (keyfn priority)]
          (PersistentPriorityMap.
           (assoc priority->set-of-items
                  priority-key
                  (conj (get priority->set-of-items priority-key #{}) item))
           (assoc item->priority item priority)
           meta
           keyfn
           nil))))

    (-contains-key? [_this item]
      (contains? item->priority item))

  IMap
    (-dissoc [this item]
      (let [priority (item->priority item ::not-found)]
        (if (= priority ::not-found)
          this
          (let [priority-key (keyfn priority)
                item-set     (priority->set-of-items priority-key)]
            (if (= (count item-set) 1)
              (PersistentPriorityMap.
               (dissoc priority->set-of-items priority-key)
               (dissoc item->priority item)
               meta
               keyfn
               nil)
              (PersistentPriorityMap.
               (assoc priority->set-of-items priority-key (disj item-set item))
               (dissoc item->priority item)
               meta
               keyfn
               nil))))))

  ISorted
    (-sorted-seq [this ascending?]
      ((if ascending? seq rseq) this))
    (-sorted-seq-from [_this k ascending?]
      (let [sets (if ascending?
                   (subseq priority->set-of-items >= k)
                   (rsubseq priority->set-of-items <= k))]
        (if keyfn
          (seq (for [[_ item-set] sets
                     item         item-set]
                 [item (item->priority item)]))
          (seq (for [[priority item-set] sets
                     item                item-set]
                 [item priority])))))
    (-entry-key [_this entry]
      (keyfn (val entry)))
    (-comparator [_this] compare)

  IFn
    (-invoke [this item]
      (-lookup this item))
    (-invoke [this item not-found]
      (-lookup this item not-found)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(set! status-im.utils.priority-map.PersistentPriorityMap.EMPTY
  (PersistentPriorityMap. (sorted-map) {} {} identity nil))

(defn- pm-empty-by
  [f-comparator]
  (PersistentPriorityMap. (sorted-map-by f-comparator) {} {} identity nil))

(defn- pm-empty-keyfn
  ([keyfn]
   (PersistentPriorityMap. (sorted-map) {} {} keyfn nil))
  ([keyfn f-comparator]
   (PersistentPriorityMap. (sorted-map-by f-comparator) {} {} keyfn nil)))

(defn- read-priority-map
  [elems]
  (if (map? elems)
    (into PersistentPriorityMap.EMPTY elems)
    (throw (js/Error "Priority map literal expects a map for its elements."))))

(register-tag-parser! "status-im.utils.priority-map" read-priority-map)

(defn priority-map
  "keyval => key val
  Returns a new priority map with supplied mappings."
  ([& keyvals]
   #_{:clj-kondo/ignore [:unresolved-symbol]}
   (loop [in  (seq keyvals)
          out status-im.utils.priority-map.PersistentPriorityMap.EMPTY]
     (if in
       (recur (nnext in) (assoc out (first in) (second in)))
       out))))

(defn priority-map-by
  "keyval => key val
  Returns a new priority map with supplied
  mappings, using the supplied comparator."
  ([f-comparator & keyvals]
   (loop [in  (seq keyvals)
          out (pm-empty-by f-comparator)]
     (if in
       (recur (nnext in) (assoc out (first in) (second in)))
       out))))

(defn priority-map-keyfn
  "keyval => key val
  Returns a new priority map with supplied
  mappings, using the supplied keyfn."
  ([keyfn & keyvals]
   (loop [in  (seq keyvals)
          out (pm-empty-keyfn keyfn)]
     (if in
       (recur (nnext in) (assoc out (first in) (second in)))
       out))))

(defn priority-map-keyfn-by
  "keyval => key val
  Returns a new priority map with supplied
  mappings, using the supplied keyfn and comparator."
  ([keyfn f-comparator & keyvals]
   (loop [in  (seq keyvals)
          out (pm-empty-keyfn keyfn f-comparator)]
     (if in
       (recur (nnext in) (assoc out (first in) (second in)))
       out))))

(def empty-message-map
  (priority-map-keyfn-by :clock-value >))

(def empty-transaction-map
  (priority-map-keyfn-by :block #(< (int %1) (int %2))))
