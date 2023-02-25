(ns schema.registry
  (:refer-clojure :exclude [merge def])
  (:require [malli.core :as malli]))

(defonce registry
  (atom (malli/default-schemas)))

(defn def
  "Defines a new schema in mutable `registry`."
  ([type ?schema]
   (swap! registry assoc type (malli/schema ?schema))
   ?schema)
  ([type props ?schema]
   (swap! registry assoc type [:schema props ?schema])
   ?schema))

(defn merge
  [& schemas]
  (apply swap! registry cljs.core/merge schemas)
  schemas)
