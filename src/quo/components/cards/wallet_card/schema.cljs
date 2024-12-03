(ns quo.components.cards.wallet-card.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:image :schema.common/image-source]
      [:title :string]
      [:subtitle :string]
      [:dismissible? {:optional true} :boolean]
      [:on-press {:optional true} fn?]
      [:on-press-close {:optional true} fn?]]]]
   :any])
