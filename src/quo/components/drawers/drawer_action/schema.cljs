(ns quo.components.drawers.drawer-action.schema)

(def ?schema
  [:=>
   [:cat
    [:map {:closed true}
     [:action {:optional true} [:maybe [:enum :arrow :toggle]]]
     [:icon {:optional true} [:maybe :keyword]]
     [:description {:optional true} [:maybe :string]]
     [:state {:optional true} [:maybe [:enum :selected]]]
     [:title {:optional true} :string]
     [:on-press {:optional true} [:maybe fn?]]
     [:customization-color {:optional true}
      [:maybe :schema.common/customization-color]]
     [:blur? {:optional true} [:maybe :boolean]]]]
   :any])
