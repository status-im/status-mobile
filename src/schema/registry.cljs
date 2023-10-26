(ns schema.registry
  (:refer-clojure :exclude [merge def])
  (:require
    [malli.core :as malli]
    malli.registry))

(defonce ^:private registry
  (atom (malli/default-schemas)))

(defn set-default-registry
  "Initializes global registry."
  []
  (malli.registry/set-default-registry! (malli.registry/mutable-registry registry)))

(defn register
  "Defines a new schema in mutable `registry`.

  We normalize `?schema` by always registering it as a proper instance of
  `malli.core/Schema` to avoid inconsistencies down the road."
  [type ?schema]
  (swap! registry assoc type (malli/schema ?schema))
  ?schema)

(defn merge
  [& schemas]
  (apply swap! registry cljs.core/merge schemas)
  schemas)
