(ns schema.re-frame
  (:require
    [schema.registry :as registry]))

(def ^:private ?cofx
  [:map])

(def ^:private ?event-fx
  [:maybe
   [:map {:closed true}
    [:db {:optional true} map?]
    [:fx {:optional true} [:vector :any]]]])

(defn register-schemas
  []
  (registry/register ::event-fx ?event-fx)
  (registry/register ::cofx ?cofx))
