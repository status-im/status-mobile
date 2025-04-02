(ns schema.quo
  (:require
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as resources]
    [schema.registry :as registry]))

(def ^:private ?customization-color (into [:enum :primary] colors/account-colors))

(def ^:private ?profile-picture-options
  [:map
   [:length :int]
   [:full-name :string]
   [:font-size :int]
   [:indicator-size {:optional true} [:maybe :int]]
   [:indicator-color {:optional true} [:maybe :string]]
   [:indicator-center-to-edge {:optional true} [:maybe :int]]
   [:theme :schema.common/theme]
   [:color :string]
   [:size :int]
   [:ring? :boolean]
   [:ring-width :int]])

(def ^:private ?account-image-uri-options
  [:map
   [:port :int]
   [:ratio :double]
   [:key-uid :string]
   [:image-name :string]
   [:theme :schema.common/theme]
   [:override-ring? [:maybe :boolean]]])

(def ^:private ?initials-image-uri-options
  [:map
   [:port :int]
   [:ratio :double]
   [:uppercase-ratio :double]
   [:font-file :string]
   [:theme :schema.common/theme]
   [:customization-color ?customization-color]
   [:key-uid {:optional true} [:maybe :string]]
   [:public-key {:optional true} [:maybe :string]]
   [:override-ring? {:optional true} [:maybe :boolean]]])

(def ^:private ?contact-image-uri-options
  [:map
   [:port :int]
   [:clock :int]
   [:ratio :double]
   [:image-name :string]
   [:public-key :string]
   [:theme :schema.common/theme]
   [:override-ring? [:maybe :boolean]]])

(def ^:private ?image-uri-config
  [:multi {:dispatch :type}
   [:account
    [:map
     [:type [:= :account]]
     [:options ?account-image-uri-options]]]
   [:contact
    [:map
     [:type [:= :contact]]
     [:options ?contact-image-uri-options]]]
   [:initials
    [:map
     [:type [:= :initials]]
     [:options ?initials-image-uri-options]]]])

(def ^:private ?profile-picture-source
  [:or
   :schema.common/image-source
   [:map
    [:config ?image-uri-config]]])

(def ^:private ?networks
  (-> (concat [:enum] resources/supported-networks)
      vec))

(defn register-schemas
  []
  (registry/register ::customization-color ?customization-color)
  (registry/register ::image-uri-config ?image-uri-config)
  (registry/register ::profile-picture-source ?profile-picture-source)
  (registry/register ::profile-picture-options ?profile-picture-options)
  (registry/register ::networks ?networks))
