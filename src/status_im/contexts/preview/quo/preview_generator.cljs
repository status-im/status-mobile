(ns status-im.contexts.preview.quo.preview-generator
  (:require
    [clojure.set :as set]
    [malli.core :as malli]
    malli.generator
    malli.util
    [status-im.contexts.preview.quo.preview :as preview]))

(defn- custom-sort-by-key
  [m]
  (let [key-order {:type -2 :customization-color -1}
        key-name  (:key m)]
    [(get key-order key-name 0) key-name]))

(defn- schema-enum->field-select
  [k {:keys [values]}]
  {:key     k
   :type    :select
   :options (map (fn [enum-member]
                   {:key enum-member})
                 values)})

(defn- schema-boolean->field-boolean
  [k]
  {:key k :type :boolean})

(defn- schema-string->field-text
  [k]
  {:key k :type :text})

(defn- schema-int->field-number
  [k]
  {:key k :type :number})

(defn- parse-leaf
  [k {:keys [type] :as node}]
  (cond
    (#{:keyword 'keyword? keyword
       :string 'string? string?}
     type)
    (schema-string->field-text k)

    (#{:boolean 'boolean? boolean?} type)
    (schema-boolean->field-boolean k)

    ;; TODO: if it's optional or nillable, add default option
    (= :enum type)
    (schema-enum->field-select k node)

    ;; TODO: handle and

    (= :or type)
    ;; Use the first parseable leaf.
    (->> (:children node)
         (keep #(parse-leaf k %))
         first)

    (#{:int 'int? int?} type)
    (schema-int->field-number k)

    (and (= :malli.core/schema type)
         (= :schema.common/customization-color (malli/form (:value node))))
    (preview/customization-color-option)

    (and (= :malli.core/schema type)
         (= :schema.common/theme (malli/form (:value node))))
    (schema-enum->field-select k {:values [:dark :light]})

    (#{'fn? fn?} type)
    nil ; ignore

    :else
    (do (tap> [:unknown-leaf k node])
        nil)))

(defn- assert-exclude-keys-exist
  "Asserts `exclude-keys` is a subset of map keys in `nodes`."
  [nodes exclude-keys]
  (when exclude-keys
    (let [available-keys (keys nodes)]
      (assert (set/subset? (set exclude-keys) (set available-keys))
              (str "Key in 'exclude-keys' is not part of the map schema: "
                   {:available-keys available-keys
                    :exclude-keys   exclude-keys})))))

(defn- parse-map
  [{nodes :keys} {:keys [exclude-keys]}]
  (assert-exclude-keys-exist nodes exclude-keys)
  (->> nodes
       (sort-by (fn [[_ v]]
                  (:order v)))
       (remove (fn [[k _]]
                 (contains? exclude-keys k)))
       (keep (fn [[k {:keys [value]}]]
               (if (= :maybe (:type value))
                 (parse-leaf k (:child value))
                 (parse-leaf k value))))))

(defn- parse-multi-dispatch
  [node options]
  (into [(schema-enum->field-select :type {:values (keys (:keys node))})]
        (mapcat (fn [[k {:keys [value]}]]
                  (cond
                    (= :merge (:type value))
                    (let [ast (->> value
                                   :children
                                   (reduce (fn [acc node-ast]
                                             (->> node-ast
                                                  malli/from-ast
                                                  (malli.util/merge acc)))
                                           nil)
                                   malli/ast)]
                      (parse-map ast options))

                    (= :map (:type value))
                    (parse-map value options)

                    :else
                    (do (tap> [:unknown-from-multi-dispatch k value])
                        nil)))
         (:keys node))))

(defn schema->descriptor
  "Nested map schemas are ignored for the moment. Although we can flatten and give
  unique descriptor keys to nested maps, this would require a few adjustments in
  the preview namespaces. Such component schemas are less common, therefore this
  function ignores this use case."
  ([form]
   (schema->descriptor form {}))
  ([form options]
   (let [ast                  (malli/ast form)
         input-type           (get-in ast [:input :type])
         children             (cond
                                (= :cat input-type)
                                (get-in ast [:input :children])

                                (= :catn input-type)
                                (->> (get-in ast [:input :keys])
                                     vals
                                     (map :value)))
         descriptors-by-key   (->> children
                                   (mapcat
                                    (fn [{:keys [type] :as child}]
                                      (cond
                                        (= :map type)
                                        (parse-map child options)

                                        (= :multi type)
                                        (parse-multi-dispatch child options))))
                                   (group-by :key))
         distinct-descriptors (-> descriptors-by-key
                                  (dissoc :accessibility-label)
                                  (update-vals first)
                                  vals)]
     (->> distinct-descriptors
          (sort-by custom-sort-by-key compare)
          (into [])))))
