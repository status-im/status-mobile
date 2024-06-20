(ns quo.components.tags.collectible-tag.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:schema.common/map {:optional true :maybe true}
      [:options [:enum :add :hold]]
      [:size [:enum :size-24 :size-32]]
      [:blur? :boolean]
      [:collectible-id :string]
      [:collectible-img-src {:no-optional true :no-maybe true}
       :schema.common/image-source]
      [:collectible-name {:no-optional true :no-maybe true}
       :string]]]]
   :any])
