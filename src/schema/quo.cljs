(ns schema.quo
  (:require
    [schema.registry :as registry]))

(def ^:private ?profile-picture-fn-params
  [:map
   [:length :int]
   [:full-name :string]
   [:font-size :int]
   [:indicator-size {:optional true} [:maybe :int]]
   [:indicator-color {:optional true} [:maybe :string]]
   [:indicator-center-to-edge {:optional true} [:maybe :int]]
   [:override-theme :schema.common/theme]
   [:background-color :string]
   [:color :string]
   [:size :int]
   [:ring? :boolean]
   [:ring-width :int]])

(def ^:private ?profile-picture-source
  [:or
   :schema.common/image-source
   [:map
    [:fn [:=> [:cat ?profile-picture-fn-params] :string]]]])

(defn register-schemas
  []
  (registry/register ::profile-picture-source ?profile-picture-source))
