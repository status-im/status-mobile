(ns quo.components.wallet.account-overview.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:account-name {:optional true} [:maybe :string]]
      [:currency-change {:optional true} [:maybe :string]]
      [:current-value {:optional true} [:maybe :string]]
      [:percentage-change {:optional true} [:maybe :string]]
      [:time-frame-string {:optional true} [:maybe :string]]
      [:time-frame-to-string {:optional true} [:maybe :string]]
      [:state {:optional true} [:enum :default :loading]]
      [:metrics {:optional true} [:enum :negative :positive]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:account {:optional true} [:enum :default :watched-address]]
      [:time-frame {:optional true}
       [:enum :one-week :one-month :three-months :one-year :all-time :custom]]]]]
   :any])
