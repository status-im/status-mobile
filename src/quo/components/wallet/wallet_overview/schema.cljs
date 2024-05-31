(ns quo.components.wallet.wallet-overview.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:state {:optional true} [:maybe [:enum :default :loading]]]
      [:time-frame {:optional true}
       [:maybe [:enum :none :selected :one-week :one-month :three-months :one-year :all-time :custom]]]
      [:metrics {:optional true} [:maybe [:enum :none :negative :positive]]]
      [:balance {:optional true} [:maybe :string]]
      [:date {:optional true} [:maybe :string]]
      [:begin-date {:optional true} [:maybe :string]]
      [:end-date {:optional true} [:maybe :string]]
      [:currency-change {:optional true} [:maybe :string]]
      [:percentage-change {:optional true} [:maybe :string]]
      [:dropdown-on-press {:optional true} [:maybe fn?]]
      [:networks {:optional true}
       [:maybe [:sequential [:map [:source [:maybe :schema.common/image-source]]]]]]
      [:dropdown-state {:optional true} [:maybe [:enum :default :disabled]]]]]]
   :any])
