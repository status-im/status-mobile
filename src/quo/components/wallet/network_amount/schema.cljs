(ns quo.components.wallet.network-amount.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:amount {:optional true} [:maybe :string]]
      [:token {:optional true} [:or :keyword :string]]]]]
   :any])
