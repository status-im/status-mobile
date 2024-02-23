(ns quo.components.list-items.token-value.schema)

(def ^private ?values
  [:map
   [:crypto-value :string]
   [:fiat-value :string]
   [:percentage-change {:optional true} :string]
   [:fiat-change {:optional true} :string]])

(def ^private ?schema
  [:=>
   [:cat
    [:map
     [:token :keyword]
     [:token-name :string]
     [:status [:enum :empty :positive :negative]]
     [:values ?values]
     [:on-press {:optional true} [:maybe fn?]]
     [:on-long-press {:optional true} [:maybe fn?]]
     [:theme :schema.common/theme]
     [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
     [:metrics? {:optional true} [:maybe :boolean]]]]
   :any])
