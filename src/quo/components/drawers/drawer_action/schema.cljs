(ns quo.components.drawers.drawer-action.schema)

(def ?schema
  [:=>
   [:cat
    [:schema.common/map {:closed true :optional true :maybe true}
     [:accessibility-label :keyword]
     [:type [:enum :main :danger]]
     [:action [:enum :arrow :toggle :input]]
     [:icon :keyword]
     [:description :string]
     [:state [:enum :selected]]
     [:title {:no-maybe true} :string]
     [:on-press fn?]
     [:input-props :map]
     [:customization-color :schema.common/customization-color]
     [:blur? :boolean]]]
   :any])
