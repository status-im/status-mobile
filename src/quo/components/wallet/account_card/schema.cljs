(ns quo.components.wallet.account-card.schema)

(def ^:private ?base
  [:schema.common/map {:optional true :maybe true}
   [:type {:no-maybe true} [:enum :default :watch-only :add-account :empty :missing-keypair]]
   [:customization-color :schema.common/customization-color]
   [:metrics? :boolean]
   [:on-press fn?]])

(def ^:private ?amount
  [:map
   [:amount {:optional true} [:maybe :string]]])

(def ^:private ?card
  [:schema.common/map {:optional true :maybe true}
   [:balance :string]
   [:loading? :boolean]
   [:name :string]
   [:percentage-value :string]
   [:emoji :string]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:multi {:dispatch :type}
      [:default [:merge ?base ?amount ?card]]
      [:watch-only [:merge ?base ?amount ?card]]
      [:missing-keypair [:merge ?base ?amount ?card]]
      [:empty [:merge ?base ?card]]
      [:add-account ?base]]]]
   :any])
