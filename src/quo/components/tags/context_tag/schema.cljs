(ns quo.components.tags.context-tag.schema)

(def ^:private ?base
  [:map
   [:type {:optional true} [:maybe [:enum :default :multiuser :group :channel :community :token :network :multinetwork :account :collectible :address :icon :audio]]]
   [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
   [:theme :schema.common/theme]
   [:blur? {:optional true} [:maybe :boolean]]
   [:state {:optional true} [:maybe [:enum :selected :default]]]])

(def ^:private ?size
  [:map
   [:size {:optional true} [:maybe [:enum 24 32]]]])

(def ^:private ?default
  [:map
   [:profile-picture {:optional true} [:maybe [:or :schema.common/image-source :string]]]
   [:full-name {:optional true} [:maybe :string]]])

(def ^:private ?multiuser
  [:map
   [:users {:optional true} [:maybe [:sequential [:map [:profile-picture {:optional true} [:maybe [:or :schema.common/image-source :string]]]
                                                       [:full-name {:optional true} [:maybe :string]]
                                                       [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]]]])

(def ^:private ?group
  [:map
   [:group-name {:optional true} [:maybe :string]]])

(def ^:private ?channel
  [:map
   [:community-name {:optional true} [:maybe :string]]
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
   [:network-logo {:optional true} [:maybe [:or :schema.common/image-source :string]]]
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
   [:collectible {:optional true} [:maybe [:or :schema.common/image-source :string]]]
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

(def ?schema
  [:=>
   [:catn
    [:props
     [:multi {:dispatch :type}
      [:default [:merge ?default ?size ?base]]
      [:multiuser [:merge ?multiuser  ?base]]
      [:group [:merge ?group ?size ?base]]
      [:channel [:merge ?channel ?size ?base]]
      [:community [:merge ?community ?size ?base]]
      [:token [:merge ?token ?size ?base]]
      [:network [:merge ?network ?base]]
      [:multinetwork [:merge ?multinetwork ?base]]
      [:account [:merge ?account ?size ?base]]
      [:collectible [:merge ?collectible ?size ?base]]
      [:address [:merge ?address ?size ?base]]
      [:icon [:merge ?icon ?size ?base]]
      [:audio [:merge ?audio ?base]]]]]
   :any])