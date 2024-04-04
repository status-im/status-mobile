(ns quo.components.wallet.token-input.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:token {:optional true} [:maybe :string :keyword]]
      [:currency {:optional true} [:maybe :string :keyword]]
      [:error? {:optional true} [:maybe :boolean]]
      [:title {:optional true} [:maybe :string]]
      [:conversion {:optional true} [:maybe :double]]
      [:show-keyboard? {:optional true} [:maybe :boolean]]
      [:networks {:optional true}
       [:maybe [:sequential [:map [:source [:maybe :schema.common/image-source]]]]]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:value {:optional true} [:maybe :string]]
      [:theme :schema.common/theme]]]]
   :any])
