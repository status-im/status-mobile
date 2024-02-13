(ns quo.components.tags.collectible-tag.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:options {:optional true} [:maybe [:enum false :add :hold]]]
      [:size {:optional true} [:enum :size-24 :size-32]]
      [:blur? {:optional true} [:maybe :boolean]]
      [:theme :schema.common/theme]
      [:collectible-img-src [:or :int :string]]
      [:collectible-name :string]
      [:collectible-id :string]
      [:container-width [:or :int :double :string]]
      [:on-layout {:optional true} [:maybe fn?]]]]]
   :any])
