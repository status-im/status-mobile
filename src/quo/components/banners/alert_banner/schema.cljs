(ns quo.components.banners.alert-banner.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:action? {:optional true} [:maybe boolean?]]
      [:text {:optional true} [:maybe string?]]
      [:container-style {:optional true} [:maybe :map]]
      [:button-text {:optional true} [:maybe string?]]
      [:on-button-press {:optional true} [:maybe fn?]]]]]
   :any])
