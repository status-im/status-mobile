(ns quo.components.wallet.network-link.schema)

(def ^:private networks [:enum :optimism :arbitrum :ethereum])

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:shape {:optional true} [:maybe [:enum :linear :1x :2x]]]
      [:source {:optional true} [:maybe networks]]
      [:destination {:optional true} [:maybe networks]]
      [:theme :schema.common/theme]]]]
   :any])