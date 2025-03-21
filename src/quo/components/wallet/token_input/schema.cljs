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
      [:on-swap {:optional true} [:maybe fn?]]
      [:container-style {:optional true} [:maybe :map]]
      [:error? [:maybe :boolean]]
      [:show-token-icon? {:optional true} [:maybe :boolean]]
      [:value [:maybe :string]]
      [:converted-value {:optional true} [:maybe :string]]
      [:swappable? {:optional true} [:maybe :boolean]]]]]
   :any])
