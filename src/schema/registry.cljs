(ns schema.registry
  (:refer-clojure :exclude [merge])
  (:require
    [malli.core :as malli]
    malli.registry))

(defonce ^:private registry
  (atom (malli/default-schemas)))

(defn init-global-registry
  []
  (malli.registry/set-default-registry! (malli.registry/mutable-registry registry)))

(defn register
  "Defines a new schema in mutable `registry`.

  We normalize `?schema` by always registering it as a proper instance of
  `malli.core/Schema` to avoid inconsistencies down the road."
  [type ?schema]
  (swap! registry assoc
    type
    ;; An into-schema instance should not be converted to schema. This will cause a
    ;; :malli.core/invalid-schema. One such schema is `:schema.common/map`.
    (if (malli/into-schema? ?schema)
      ?schema
      (malli/schema ?schema)))
  ?schema)

(defn merge
  [& schemas]
  (apply swap! registry cljs.core/merge schemas)
  schemas)
