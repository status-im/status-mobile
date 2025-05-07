(ns quo.components.wallet.transaction-progress.schema)

(def ^:private ?network
  [:map
   [:network {:optional true} [:maybe :keyword]]
   [:full-name {:optional true} [:maybe :string]]
   [:state {:optional true} [:maybe [:enum :pending :sending :confirmed :finalising :finalized :error]]]
   [:counter {:optional true} [:maybe :int]]
   [:total-box {:optional true} [:maybe :int]]
   [:epoch-number {:optional true} [:maybe :string]]
   [:progress {:optional true} [:maybe :int]]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:title {:optional true} [:maybe :string]]
      [:network {:optional true} [:maybe :keyword]]
      [:tag-name {:optional true} [:maybe :string]]
      [:tag-number {:optional true} [:maybe [:or :string :int]]]
      [:tag-photo {:optional true} [:maybe :schema.common/image-source]]
      [:on-press {:optional true} [:maybe fn?]]
      [:networks {:optional true} [:maybe [:sequential ?network]]]]]]
   :any])
