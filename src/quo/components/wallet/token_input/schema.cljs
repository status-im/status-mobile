(ns quo.components.wallet.token-input.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:token-symbol [:maybe [:or :string :keyword]]]
      [:currency-symbol [:maybe [:or :string :keyword]]]
      [:hint-component {:optional true} [:maybe :schema.common/hiccup]]
      [:on-token-press {:optional true} [:maybe fn?]]
      [:on-swap [:maybe fn?]]
      [:container-style {:optional true} [:maybe :map]]
      [:error? [:maybe :boolean]]
      [:value [:maybe :string]]
      [:converted-value [:maybe :string]]]]]
   :any])
