(ns schema.common
  (:require
    [malli.core :as malli]
    malli.util
    [schema.registry :as registry]))

(defn- optional-keys
  "Makes map keys optional, non-recursively.

  Ignores keys with schema property :no-maybe true."
  [?schema]
  (let [mapper (fn [[_k properties :as entry]]
                 (if (:no-optional properties)
                   (update entry 1 dissoc :no-optional)
                   (update entry 1 assoc :optional true)))]
    (malli.util/transform-entries ?schema #(map mapper %) nil)))

(defn- maybe-keys
  "Makes map keys nullable, non-recursively.

  Ignores keys with schema property :no-maybe true."
  [?schema]
  (let [mapper (fn [[_k properties ?key :as entry]]
                 (let [key-form (malli/form ?key)]
                   (if (or (:no-maybe properties)
                           (and (vector? key-form)
                                (= :maybe (first key-form))))
                     (update entry 1 dissoc :no-maybe)
                     (assoc entry 2 [:maybe key-form]))))]
    (malli.util/transform-entries ?schema #(map mapper %) nil)))

(defn- ?map-optional-maybe
  "This implementation should closely track how the base schema `:map` is
  implemented by malli. This is a solution to sort of inherit a base schema and
  reuse its implementation, but extend it to support new features.

  Usage:

  [:schema.common/map {:optional true :maybe true}
   [:type [:enum :default :multiuser :group]]
   [:customization-color :schema.common/customization-color]
   [:blur? boolean?]
   [:value {:no-optional true} pos-int?]]

  This is the same as if you manually entered the following:

  [:map
   [:type {:optional true} [:maybe [:enum :default :multiuser :group]]]
   [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
   [:blur? {:optional true} [:maybe :boolean]]
   [:value [:maybe pos-int?]]]

  You can verify the final transformed `:schema.common/map` by
  calling `(malli.core/form ?some-schema)`.

  Schema `:schema.common/map` may take two properties:

  - When `:optional` is non-nil, every key will have its `:optional` property
  set to true. If any key uses the property `:no-optional` non-nil, the key is
  ignored.

  - When `:maybe` is non-nil, every key will have its children wrapped by a
  `:maybe` schema. If any key uses the property `:no-maybe` non-nil, the key is
  ignored.
  "
  []
  ^{:type ::malli/into-schema}
  (reify
   malli/AST
     (-from-ast [parent ast options]
       (malli/-from-ast parent ast options))

   malli/IntoSchema
     (-type [_]
       :schema.common/map)
     (-type-properties [this]
       (malli/-type-properties this))
     (-properties-schema [this options]
       (malli/-properties-schema this options))
     (-children-schema [this options]
       (malli/-children-schema this options))
     (-into-schema [_parent props children options]
       (cond-> (malli/into-schema :map (dissoc props :optional :maybe) children options)
         (:optional props) (optional-keys)
         (:maybe props)    (maybe-keys)))))

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

(def ^:private ?exception
  [:fn {:error/message "schema.common/exception should be of type ExceptionInfo"}
   (fn [v] (instance? ExceptionInfo v))])

(def ^:private ?hiccup
  vector?)

(defn register-schemas
  []
  (registry/register ::theme ?theme)
  (registry/register ::customization-color ?customization-color)
  (registry/register ::public-key ?public-key)
  (registry/register ::image-source ?image-source)
  (registry/register ::rpc-call ?rpc-call)
  (registry/register ::exception ?exception)
  (registry/register ::map (?map-optional-maybe))
  (registry/register ::hiccup ?hiccup))
