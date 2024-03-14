(ns schema.quo
  (:require
    [schema.registry :as registry]))

(def ^:private ?profile-picture-options
  [:map
   [:length :int]
   [:full-name :string]
   [:font-size :int]
   [:indicator-size {:optional true} [:maybe :int]]
   [:indicator-color {:optional true} [:maybe :string]]
   [:indicator-center-to-edge {:optional true} [:maybe :int]]
   [:theme {:optional true} [:maybe :schema.common/theme]]
   [:background-color :string]
   [:color :string]
   [:size :int]
   [:ring? :boolean]
   [:ring-width :int]])

(def ^:private ?account-image-uri-opts
  [:map
   [:port :int]
   [:ratio :double]
   [:key-uid :string]
   [:image-name :string]
   [:theme :schema.common/theme]
   [:override-ring? [:maybe :boolean]]])

(def ^:private ?initials-image-uri-opts
  [:map
   [:port :int]
   [:ratio :double]
   [:uppercase-ratio :double]
   [:font-file :string]
   [:theme :schema.common/theme]
   [:key-uid {:optional true} [:maybe :string]]
   [:public-key {:optional true} [:maybe :string]]
   [:override-ring? {:optional true} [:maybe :boolean]]])

(def ^:private ?contact-image-uri-opts
  [:map
   [:port :int]
   [:clock :int]
   [:ratio :double]
   [:image-name :string]
   [:public-key :string]
   [:theme :schema.common/theme]
   [:override-ring? [:maybe :boolean]]])

(def ?image-uri-config
  [:or
   [:map
    [:kind [:enum :contact]]
    [:options ?contact-image-uri-opts]]
   [:map
    [:kind [:enum :account]]
    [:options ?account-image-uri-opts]]
   [:map
    [:kind [:enum :initials]]
    [:options ?initials-image-uri-opts]]])

(def ^:private ?profile-picture-source
  [:or
   :schema.common/image-source
   [:map
    [:config ?image-uri-config]]])

(defn register-schemas
  []
  (registry/register ::profile-picture-options ?profile-picture-options)
  (registry/register ::image-uri-config ?image-uri-config)
  (registry/register ::profile-picture-source ?profile-picture-source))
