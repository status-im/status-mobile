(ns quo.components.wallet.address-text.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:address :string]
      [:blur? {:optional true} [:maybe :boolean]]
      [:format {:optional true} [:enum :short :long]]
      [:theme {:optional true} :schema.common/theme]
      [:networks {:optional true} [:sequential [:map [:name :keyword] [:short-name :string]]]]]]]
   :any])
