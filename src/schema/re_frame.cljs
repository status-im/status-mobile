(ns schema.re-frame
  (:require
    [schema.registry :as registry]))

(def ^:private ?cofx
  [:map])

(defn register-schemas
  []
  (registry/register ::cofx ?cofx))
