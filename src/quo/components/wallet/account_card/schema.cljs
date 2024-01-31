(ns quo.components.wallet.account-card.schema)

(def ?base
  [:map {:closed true}
   [:type {:optional true} [:enum :default :watch-only :add-account :empty :missing-keypair]]
   [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
   [:theme :schema.common/theme]
   [:metrics? {:optional true} [:maybe :boolean]]
   [:on-press {:optional true} [:maybe fn?]]])

(def ?amount
  [:amount {:optional true} [:maybe :string]])

(def ?card
  [:map
   [:balance {:optional true} [:maybe :string]]
   [:loading? {:optional true} [:maybe :boolean]]
   [:name {:optional true} [:maybe :string]]
   [:percentage-value {:optional true} [:maybe :string]]
   [:emoji {:optional true} [:maybe :string]]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:multi {:dispatch :type}
      [:default [:merge ?base ?amount ?card]]
      [:watch-only [:merge ?base ?amount ?card]]
      [:missing-keypair [:merge ?base ?amount ?card]]
      [:empty [:merge ?base ?card]]
      [:add-account [:merge ?base]]]]]
   :any])