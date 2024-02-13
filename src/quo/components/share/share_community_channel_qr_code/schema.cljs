(ns quo.components.share.share-community-channel-qr-code.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:qr-image-uri :string]
      [:emoji {:optional true} [:maybe :string]]
      [:full-name :string]
      [:customization-color :string]
      [:theme :schema.common/theme]]]]
   :any])
