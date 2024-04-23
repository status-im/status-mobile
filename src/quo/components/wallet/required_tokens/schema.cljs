(ns quo.components.wallet.required-tokens.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:type [:enum :token :collectible]]
      [:amount {:optional true} [:maybe [:or :string :int]]]
      [:token {:optional true} [:maybe :string]]
      [:token-img-src {:optional true} [:maybe :schema.common/image-source]]
      [:collectible-img-src {:optional true} [:maybe :schema.common/image-source]]
      [:collectible-name {:optional true} [:maybe :string]]
      [:divider? {:optional true} [:maybe :boolean]]
      [:container-style {:optional true} [:maybe :map]]]]]
   :any])
