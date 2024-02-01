(ns quo.components.wallet.address-text.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:address :string]
      [:blur? {:optional true} [:maybe :boolean]]
      [:format {:optional true} [:enum :short :long]]
      [:theme :schema.common/theme]
      [:networks {:optional true} [:sequential [:map {:closed true} [:name :keyword] [:short-name :string]]]]]]]
   :any])