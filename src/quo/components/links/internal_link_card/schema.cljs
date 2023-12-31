(ns quo.components.links.internal-link-card.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:title {:optional true} [:maybe :string]]
      [:description {:optional true} [:maybe :string]]
      [:channel-name {:optional true} [:maybe :string]]
      [:loading? {:optional true} [:maybe :boolean]]
      [:subtitle {:optional true} [:maybe :string]]
      [:icon {:optional true} [:maybe [:or :string :int]]]
      [:banner {:optional true} [:maybe [:or :string :int]]]
      [:type {:optional true} [:maybe :keyword]]
      [:on-press {:optional true} [:maybe fn?]]
      [:members-count {:optional true} [:maybe [:or :int :string]]]
      [:active-members-count {:optional true} [:maybe [:or :int :string]]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:emojis {:optional true} [:maybe [:vector :keyword]]]
      [:theme :schema.common/theme]]]]
   :any])
