(ns quo.components.list-items.token-network.schema)

(def ^private ?schema
  [:=>
   [:cat
    [:map
     [:token :keyword]
     [:label :string]
     [:token-value :string]
     [:fiat-value :string]
     [:networks [:* [:map [:source :schema.common/image-source]]]]
     [:on-press {:optional true} [:maybe fn?]]
     [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
     [:state {:optional true} [:enum :default :active :selected]]
     [:theme :schema.common/theme]]]
   :any])
