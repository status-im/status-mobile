(ns quo.components.list-items.status-list-item.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:status [:enum :positive :negative]]
      [:title [:maybe string?]]
      [:description {:optional true} [:maybe string?]]
      [:blur? {:optional true} [:maybe boolean?]]]]]
   :any])
