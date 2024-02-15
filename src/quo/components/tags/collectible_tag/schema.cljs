(ns quo.components.tags.collectible-tag.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:options {:optional true} [:maybe [:enum :add :hold]]]
      [:size {:optional true} [:maybe [:enum :size-24 :size-32]]]
      [:blur? {:optional true} [:maybe boolean?]]
      [:theme :schema.common/theme]
      [:collectible-img-src :schema.common/image-source]
      [:collectible-name string?]
      [:collectible-id string?]]]]
   :any])
