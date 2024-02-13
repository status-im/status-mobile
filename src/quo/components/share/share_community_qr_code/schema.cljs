(ns quo.components.share.share-community-qr-code.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:qr-image-uri :string]
      [:emoji :string]
      [:theme :schema.common/theme]]]]
   :any])