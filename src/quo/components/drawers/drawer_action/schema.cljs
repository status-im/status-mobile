(ns quo.components.drawers.drawer-action.schema)

(def ?schema
  [:=>
   [:cat
    [:map {:closed true}
     [:accessibility-label {:optional true} [:maybe :keyword]]
     [:type {:optional true} [:maybe [:enum :main :danger]]]
     [:action {:optional true} [:maybe [:enum :arrow :toggle :input]]]
     [:icon {:optional true} [:maybe :keyword]]
     [:description {:optional true} [:maybe :string]]
     [:state {:optional true} [:maybe [:enum :selected]]]
     [:title {:optional true} :string]
     [:on-press {:optional true} [:maybe fn?]]
     [:input-props {:optional true} [:maybe :map]]
     [:customization-color {:optional true}
      [:maybe :schema.common/customization-color]]
     [:blur? {:optional true} [:maybe :boolean]]]]
   :any])
