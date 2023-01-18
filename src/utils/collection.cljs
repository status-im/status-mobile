(ns utils.collection)

(defn first-index
  "Returns first index in coll where predicate on coll element is truthy"
  [pred coll]
  (->> coll
       (keep-indexed (fn [idx e]
                       (when (pred e)
                         idx)))
       first))

(defn index-by
  "Given a collection and a unique key function, returns a map that indexes the collection.
  Similar to group-by except that the map values are single objects (depends on key uniqueness)."
  [key coll]
  (into {} (map #(vector (key %) %) coll)))

(defn distinct-by
  "Given a key and a collection returns a unique collection by that key"
  [key coll]
  (let [groups (group-by key coll)]
    (map #(first (groups %)) (distinct (map key coll)))))

(defn map-keys
  [f m]
  (->> (map (fn [[k v]] [(f k) v]) m)
       (into {})))
