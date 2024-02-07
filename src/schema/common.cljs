(ns schema.common
  (:require
    [schema.registry :as registry]))

(def ^:private ?theme
  [:enum :light :dark])

(def ^:private ?customization-color
  [:or :string :keyword])

(def ^:private ?public-key
  [:re #"^0x04[0-9a-f]{128}$"])

(def ^:private ?image-source
  [:or
   :int
   :string
   [:map
    [:uri [:maybe [:string]]]]])

(def ^:private ?rpc-call
  [:sequential
   {:min 1}
   [:map
    {:closed true}
    [:method [:or :keyword :string]]
    [:params [:sequential :any]]
    [:js-response {:optional true} :any]
    [:on-success [:or fn? [:cat keyword? [:* :any]]]]
    [:on-error [:or fn? [:cat keyword? [:* :any]]]]]])

(def ^:private ?error
  [:fn {:error/message "schema.common/error should be of type js/Error"}
   (fn [v] (instance? js/Error v))])

(defn register-schemas
  []
  (registry/register ::theme ?theme)
  (registry/register ::customization-color ?customization-color)
  (registry/register ::public-key ?public-key)
  (registry/register ::image-source ?image-source)
  (registry/register ::rpc-call ?rpc-call)
  (registry/register ::error ?error))
