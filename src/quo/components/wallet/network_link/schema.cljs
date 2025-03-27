(ns quo.components.wallet.network-link.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:shape {:optional true} [:maybe [:enum :linear :1x :2x]]]
      [:source {:optional true} [:maybe :schema.quo/networks]]
      [:destination {:optional true} [:maybe :schema.quo/networks]]]]]
   :any])
