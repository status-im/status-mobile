(ns quo.components.list-items.token-info.schema)

(def ?schema
  [:=>
   [:cat
    [:map
     [:token [:or :string :keyword]]
     [:label :string]
     [:on-press {:optional true} [:maybe fn?]]
     [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
     [:state {:optional true} [:maybe [:enum :default :active :selected :disabled]]]]]
   :any])
