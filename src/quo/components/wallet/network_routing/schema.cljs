(ns quo.components.wallet.network-routing.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:networks {:optional true}
       [:maybe
        [:sequential
         [:map
          [:amount :int]
          [:max-amount :int]
          [:network-name [:or :string :keyword]]]]]]
      [:container-style {:optional true} [:maybe :map]]]]]
   :any])
