(ns quo.components.inputs.amount-input.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:status {:optional true} [:enum :default :error]]
      [:theme :schema.common/theme]
      [:on-change-text {:optional true} [:maybe fn?]]
      [:container-style {:optional true} [:maybe :map]]
      [:auto-focus {:optional true} [:maybe :boolean]]
      [:min-value {:optional true} [:maybe :int]]
      [:max-value {:optional true} [:maybe :int]]
      [:return-key-type {:optional true} [:maybe :string]]
      [:init-value {:optional true} [:maybe :int]]]]]
   :any])
