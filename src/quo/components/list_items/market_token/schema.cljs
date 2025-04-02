(ns quo.components.list-items.market-token.schema)

(def ?schema
  [:=>
   [:cat
    [:map
     [:token-name :string]
     [:token-rank :int]
     [:market-cap :string]
     [:price :string]
     [:percentage-change :double]
     [:on-press {:optional true} [:maybe fn?]]
     [:on-long-press {:optional true} [:maybe fn?]]
     [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]
   :any])
