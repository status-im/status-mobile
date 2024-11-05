(ns quo.components.wallet.progress-bar.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:progressed-value {:optional true} [:maybe [:or :string :int]]]
      [:full-width? {:optional true} [:maybe :boolean]]]]]
   :any])
