(ns quo.components.list-items.token-network.schema)

(def ?schema
  [:=>
   [:cat
    [:map
     [:token [:or :string :keyword]]
     [:label :string]
     [:token-value :string]
     [:fiat-value :string]
     [:networks [:* [:map [:source :schema.common/image-source]]]]
     [:on-press {:optional true} [:maybe fn?]]
     [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
     [:state {:optional true} [:maybe [:enum :default :active :selected :disabled]]]]]
   :any])
