(ns status-im.utils.priority-map
  (:require [cljs.core :as core])
  (:use [cljs.reader :only [register-tag-parser!]])
  (:require-macros [cljs.core :as coreclj]))

(deftype PersistentPriorityMap [priority->set-of-items item->priority meta keyfn ^:mutable __hash]
  IPrintWithWriter
  (-pr-writer [coll writer opts]
    (let [pr-pair (fn [keyval] (pr-sequential-writer writer pr-writer "" " " "" opts keyval))]
      (pr-sequential-writer writer pr-pair "#status-im.utils.priority-map {" ", " "}" opts coll)))

  IWithMeta
  (-with-meta [this meta]
    (PersistentPriorityMap. priority->set-of-items item->priority meta keyfn __hash))

  IMeta
  (-meta [this] meta)

  ICollection
  (-conj [this entry]
    (if (vector? entry)
      (-assoc this (-nth entry 0) (-nth entry 1))
      (reduce -conj this entry)))

  IEmptyableCollection
  (-empty [this] (with-meta
                   status-im.utils.priority-map.PersistentPriorityMap.EMPTY
                   meta))

  IEquiv
  (-equiv [this other]
    (-equiv item->priority other))

  IHash
  (-hash [this]
    (coreclj/caching-hash this core/hash-unordered-coll __hash))

  ISeqable
  (-seq [this]
    (if keyfn
      (seq (for [[priority item-set] priority->set-of-items, item item-set]
             (MapEntry. item (item->priority item))))
      (seq (for [[priority item-set] priority->set-of-items, item item-set]
             (MapEntry. item priority)))))

  IReversible
  (-rseq [coll]
    (if keyfn
      (seq (for [[priority item-set] (rseq priority->set-of-items), item item-set]
             (MapEntry. item (item->priority item))))
      (seq (for [[priority item-set] (rseq priority->set-of-items), item item-set]
             (MapEntry. item priority)))))

  ICounted
  (-count [this]
    (count item->priority))

  ILookup
  (-lookup [this item]
    (get item->priority item))
  (-lookup [coll item not-found]
    (get item->priority item not-found))

  IStack
  (-peek [this]
    (when-not (zero? (count item->priority))
      (let [f (first priority->set-of-items)
            item (first (val f))]
        (if keyfn
          [item (item->priority item)]
          [item (key f)]))))
  (-pop [this]
    (if (zero? (count item->priority))
      (throw (js/Error. "Can't pop empty priority map"))
      (let [f (first priority->set-of-items)
            item-set (val f)
            item (first item-set)
            priority-key (key f)]
        (if (= (count item-set) 1)
          (PersistentPriorityMap.
           (dissoc priority->set-of-items priority-key)
           (dissoc item->priority item)
           meta
           keyfn
           nil)
          (PersistentPriorityMap.
           (assoc priority->set-of-items priority-key (disj item-set item)),
           (dissoc item->priority item)
           meta
           keyfn
           nil)))))

  IAssociative
  (-assoc [this item priority]
    (if-let [current-priority (get item->priority item nil)]
      (if (= current-priority priority)
        this
        (let [priority-key (keyfn priority)
              current-priority-key (keyfn current-priority)
              item-set (get priority->set-of-items current-priority-key)]
          (if (= (count item-set) 1)
            (PersistentPriorityMap.
             (assoc (dissoc priority->set-of-items current-priority-key)
                    priority-key (conj (get priority->set-of-items priority-key #{}) item))
             (assoc item->priority item priority)
             meta
             keyfn
             nil)
            (PersistentPriorityMap.
             (assoc priority->set-of-items
                    current-priority-key (disj (get priority->set-of-items current-priority-key) item)
                    priority-key (conj (get priority->set-of-items priority-key #{}) item))
             (assoc item->priority item priority)
             meta
             keyfn
             nil))))
      (let [priority-key (keyfn priority)]
        (PersistentPriorityMap.
         (assoc priority->set-of-items
                priority-key (conj (get priority->set-of-items priority-key #{}) item))
         (assoc item->priority item priority)
         meta
         keyfn
         nil))))

  (-contains-key? [this item]
    (contains? item->priority item))

  IMap
  (-dissoc [this item]
    (let [priority (item->priority item ::not-found)]
      (if (= priority ::not-found)
        this
        (let [priority-key (keyfn priority)
              item-set (priority->set-of-items priority-key)]
          (if (= (count item-set) 1)
            (PersistentPriorityMap.
             (dissoc priority->set-of-items priority-key)
             (dissoc item->priority item)
             meta
             keyfn
             nil)
            (PersistentPriorityMap.
             (assoc priority->set-of-items priority-key (disj item-set item)),
             (dissoc item->priority item)
             meta
             keyfn
             nil))))))

  ISorted
  (-sorted-seq [this ascending?]
    ((if ascending? seq rseq) this))
  (-sorted-seq-from [this k ascending?]
    (let [sets (if ascending?
                 (subseq priority->set-of-items >= k)
                 (rsubseq priority->set-of-items <= k))]
      (if keyfn
        (seq (for [[priority item-set] sets, item item-set]
               [item (item->priority item)]))
        (seq (for [[priority item-set] sets, item item-set]
               [item priority])))))
  (-entry-key [this entry]
    (keyfn (val entry)))
  (-comparator [this] compare)

  IFn
  (-invoke [this item]
    (-lookup this item))
  (-invoke [this item not-found]
    (-lookup this item not-found)))

(set! status-im.utils.priority-map.PersistentPriorityMap.EMPTY
      (PersistentPriorityMap. (sorted-map) {} {} identity nil))

(defn- pm-empty-by [comparator]
  (PersistentPriorityMap. (sorted-map-by comparator) {} {} identity nil))

(defn- pm-empty-keyfn
  ([keyfn] (PersistentPriorityMap. (sorted-map) {} {} keyfn nil))
  ([keyfn comparator] (PersistentPriorityMap. (sorted-map-by comparator) {} {} keyfn nil)))

(defn- read-priority-map [elems]
  (if (map? elems)
    (into status-im.utils-map.PersistentPriorityMap.EMPTY elems)
    (throw (js/Error "Priority map literal expects a map for its elements."))))

(register-tag-parser! "status-im.utils.priority-map" read-priority-map)

(defn priority-map
  "keyval => key val
  Returns a new priority map with supplied mappings."
  ([& keyvals]
   (loop [in (seq keyvals) out status-im.utils.priority-map.PersistentPriorityMap.EMPTY]
     (if in
       (recur (nnext in) (assoc out (first in) (second in)))
       out))))

(defn priority-map-by
  "keyval => key val
  Returns a new priority map with supplied
  mappings, using the supplied comparator."
  ([comparator & keyvals]
   (loop [in (seq keyvals) out (pm-empty-by comparator)]
     (if in
       (recur (nnext in) (assoc out (first in) (second in)))
       out))))

(defn priority-map-keyfn
  "keyval => key val
  Returns a new priority map with supplied
  mappings, using the supplied keyfn."
  ([keyfn & keyvals]
   (loop [in (seq keyvals) out (pm-empty-keyfn keyfn)]
     (if in
       (recur (nnext in) (assoc out (first in) (second in)))
       out))))

(defn priority-map-keyfn-by
  "keyval => key val
  Returns a new priority map with supplied
  mappings, using the supplied keyfn and comparator."
  ([keyfn comparator & keyvals]
   (loop [in (seq keyvals) out (pm-empty-keyfn keyfn comparator)]
     (if in
       (recur (nnext in) (assoc out (first in) (second in)))
       out))))
