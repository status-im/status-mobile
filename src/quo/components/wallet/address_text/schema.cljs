(ns quo.components.wallet.address-text.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:address {:optional true} [:maybe :string]]
      [:blur? {:optional true} [:maybe :boolean]]
      [:format {:optional true} [:enum :short :long]]
      [:theme :schema.common/theme]
      [:networks {:optional true}
       [:maybe [:sequential [:map [:network-name :keyword] [:short-name :string]]]]]
      [:full-address? {:optional true} [:maybe :boolean]]]]]
   :any])
