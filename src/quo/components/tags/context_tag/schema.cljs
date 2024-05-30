(ns quo.components.tags.context-tag.schema
  (:require [malli.core :as malli]))

(def ^:private ?context-base
  [:map
   [:type {:optional true}
    [:maybe
     [:enum :default :multiuser :group :channel :community :token :network :multinetwork :account
      :collectible :address :icon :audio :wallet-user]]]
   [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
   [:container-style {:optional true} [:maybe :map]]
   [:blur? {:optional true} [:maybe :boolean]]
   [:state {:optional true} [:maybe [:enum :selected :default]]]])

(def ^:private ?size
  [:map
   [:size {:optional true} [:maybe [:enum 24 32]]]])

(def ^:private ?default
  [:map
   [:profile-picture {:optional true} [:maybe :schema.quo/profile-picture-source]]
   [:full-name {:optional true} [:maybe :string]]])

(def ^:private ?multiuser
  [:map
   [:users {:optional true}
    [:maybe
     [:sequential
      [:map
       [:profile-picture {:optional true} [:maybe :schema.quo/profile-picture-source]]
       [:full-name {:optional true} [:maybe :string]]
       [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]]]])

(def ^:private ?group
  [:map
   [:group-name {:optional true} [:maybe :string]]])

(def ^:private ?channel
  [:map
   [:community-name {:optional true} [:maybe :string]]
   [:community-logo {:optional true} [:maybe :schema.common/image-source]]
   [:channel-name {:optional true} [:maybe :string]]])

(def ^:private ?community
  [:map
   [:community-name {:optional true} [:maybe :string]]])

(def ^:private ?token
  [:map
   [:amount {:optional true} [:maybe [:or :string :int]]]
   [:token {:optional true} [:maybe :string]]])

(def ^:private ?network
  [:map
   [:network-logo {:optional true} [:maybe :schema.common/image-source]]
   [:network-name {:optional true} [:maybe :string]]])

(def ^:private ?multinetwork
  [:map
   [:networks {:optional true} [:maybe [:sequential ?network]]]])

(def ^:private ?account
  [:map
   [:account-name {:optional true} [:maybe :string]]
   [:emoji {:optional true} [:maybe :string]]])

(def ^:private ?collectible
  [:map
   [:collectible {:optional true} [:maybe :schema.common/image-source]]
   [:collectible-name {:optional true} [:maybe :string]]
   [:collectible-number {:optional true} [:maybe [:or :string :int]]]])

(def ^:private ?address
  [:map
   [:address {:optional true} [:maybe :string]]])

(def ^:private ?icon
  [:map
   [:icon {:optional true} [:maybe :keyword]]
   [:context {:optional true} [:maybe :string]]])

(def ^:private ?audio
  [:map
   [:duration {:optional true} [:maybe :string]]])

(def ^:private ?wallet-user
  [:map
   [:full-name {:optional true} [:maybe :string]]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:multi {:dispatch :type}
      [::malli/default [:merge ?default ?size ?context-base]]
      [:default [:merge ?default ?size ?context-base]]
      [:multiuser [:merge ?multiuser ?context-base]]
      [:group [:merge ?group ?size ?context-base]]
      [:channel [:merge ?channel ?size ?context-base]]
      [:community [:merge ?community ?size ?context-base]]
      [:token [:merge ?token ?size ?context-base]]
      [:network [:merge ?network ?context-base]]
      [:multinetwork [:merge ?multinetwork ?context-base]]
      [:account [:merge ?account ?size ?context-base]]
      [:collectible [:merge ?collectible ?size ?context-base]]
      [:address [:merge ?address ?size ?context-base]]
      [:icon [:merge ?icon ?size ?context-base]]
      [:audio [:merge ?audio ?context-base]]
      [:wallet-user [:merge ?wallet-user ?size ?context-base]]]]]
   :any])
