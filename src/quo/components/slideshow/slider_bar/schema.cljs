(ns quo.components.slideshow.slider-bar.schema)

(def ?schema
  [:=>
   [:cat
    [:map {:closed true}
     [:total-amount {:optional true} [:maybe :int]]
     [:active-index {:optional true} :int]
     [:customization-color {:optional true}
      [:maybe :schema.common/customization-color]]
     [:blur? {:optional true} [:maybe :boolean]]
     [:accessibility-label {:optional true} [:maybe :keyword]]
     [:container-style {:optional true} [:maybe :map]]]]
   :any])
