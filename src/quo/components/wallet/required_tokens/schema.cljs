(ns quo.components.wallet.required-tokens.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:schema.common/map {:closed true :optional true :maybe true}
      [:type {:no-maybe true} [:enum :token :collectible]]
      [:amount [:or :string :int]]
      [:token :string]
      [:token-img-src :schema.common/image-source]
      [:collectible-img-src :schema.common/image-source]
      [:collectible-name :string]
      [:divider? :boolean]
      [:container-style :map]]]]
   :any])
