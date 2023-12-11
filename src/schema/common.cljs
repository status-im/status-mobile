(ns schema.common
  (:require
    [schema.registry :as registry]))

(def ^:private ?theme
  [:enum :light :dark])

(def ^:private ?customization-color
  [:or :string :keyword])

(def ^:private ?public-key
  [:re #"^0x04[0-9a-f]{128}$"])

(def ^:private ?rpc-call
  [:sequential
   {:min 1}
   [:map
    {:closed true}
    [:method :string]
    [:params [:sequential :any]]
    [:js-response {:optional true} :any]
    [:on-success [:or fn? [:cat keyword? [:* :any]]]]
    [:on-error [:or fn? [:cat keyword? [:* :any]]]]]])

(defn register-schemas
  []
  (registry/register ::theme ?theme)
  (registry/register ::customization-color ?customization-color)
  (registry/register ::public-key ?public-key)
  (registry/register ::rpc-call ?rpc-call))
