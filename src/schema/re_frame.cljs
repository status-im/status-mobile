(ns schema.re-frame
  (:require
    [schema.registry :as registry]))

(def ^:private ?cofx
  [:map])

(def ^:private ?event
  [:and
   [:vector {:min 1} :any]
   [:catn
    [:event-id keyword?]
    [:event-args [:* :any]]]])

(defn register-schemas
  []
  (registry/register ::cofx ?cofx)
  (registry/register ::event ?event))
