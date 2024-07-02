(ns quo.components.wallet.amount-input.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:status {:optional true} [:maybe [:enum :default :error]]]
      [:on-inc-press {:optional true} [:maybe fn?]]
      [:on-dec-press {:optional true} [:maybe fn?]]
      [:container-style {:optional true} [:maybe :map]]
      [:min-value {:optional true} [:maybe :int]]
      [:max-value {:optional true} [:maybe :int]]
      [:value [:maybe :int]]]]]
   :any])
