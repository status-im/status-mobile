(ns quo.components.tags.collectible-tag.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:options {:optional true} [:maybe [:enum :add :hold]]]
      [:size {:optional true} [:maybe [:enum :size-24 :size-32]]]
      [:blur? {:optional true} [:maybe :boolean]]
      [:theme :schema.common/theme]
      [:collectible-img-src [:maybe :schema.common/image-source :string]]
      [:collectible-name :string]
      [:collectible-id {:optional true} [:maybe :string]]]]]
   :any])
