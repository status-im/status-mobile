(ns schema.common
  (:require
    [schema.registry :as registry]))

(def ^:private ?theme
  [:enum :light :dark])

(def ^:private ?customization-color
  [:or :string :keyword])

(def ^:private ?public-key
  [:re #"^0x04[0-9a-f]{128}$"])

(def ^:private ?general-rpc-call
  [:cat
   [:map
    [:method :string]
    [:params [:sequential :any]]
    [:on-success fn?]
    [:on-error fn?]]])

(defn register-schemas
  []
  (registry/register ::theme ?theme)
  (registry/register ::customization-color ?customization-color)
  (registry/register ::public-key ?public-key)
  (registry/register ::general-rpc-call ?general-rpc-call))
