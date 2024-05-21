(ns quo.components.buttons.swap-order-button.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:disabled? {:optional true} [:maybe :boolean]]
      [:on-press fn?]
      [:container-style {:optional true} [:maybe :any]]]]]
   :any])
