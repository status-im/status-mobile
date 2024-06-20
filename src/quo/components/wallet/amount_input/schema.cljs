(ns quo.components.wallet.amount-input.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:schema.common/map {:closed true :optional true :maybe true}
      [:status [:enum :default :error]]
      [:on-inc-press fn?]
      [:on-dec-press fn?]
      [:container-style :map]
      [:min-value :int]
      [:max-value :int]
      [:value {:no-optional true} :int]]]]
   :any])
