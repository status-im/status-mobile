(ns quo.components.tags.collectible-tag.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:options {:optional true} [:maybe [:enum :add :hold]]]
      [:size {:optional true} [:maybe [:enum :size-24 :size-32]]]
      [:blur? {:optional true} [:maybe :boolean]]
      [:collectible-img-src :schema.common/image-source]
      [:collectible-name :string]
      [:collectible-id {:optional true} [:maybe :string]]]]]
   :any])
