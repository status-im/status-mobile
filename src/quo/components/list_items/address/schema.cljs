(ns quo.components.list-items.address.schema)

(def ?schema
  [:=>
   [:cat
    [:map
     [:address :string]
     [:networks {:optional true} [:* [:map [:network-name :keyword] [:short-name :string]]]]
     [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
     [:on-press {:optional true} [:maybe fn?]]
     [:blur? {:optional true} [:maybe :boolean]]
     [:active-state? {:optional true} [:maybe :boolean]]]]
   :any])
