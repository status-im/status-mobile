(ns quo.components.list-items.token-value.schema)

(def ^:private ?values
  [:map
   [:crypto-value :string]
   [:fiat-value :string]
   [:percentage-change {:optional true} :string]
   [:fiat-change {:optional true} :string]])

(def ?schema
  [:=>
   [:cat
    [:map
     [:token [:or keyword? string?]]
     [:token-name :string]
     [:status [:enum :empty :positive :negative]]
     [:values ?values]
     [:on-press {:optional true} [:maybe fn?]]
     [:on-long-press {:optional true} [:maybe fn?]]
     [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
     [:metrics? {:optional true} [:maybe :boolean]]]]
   :any])
