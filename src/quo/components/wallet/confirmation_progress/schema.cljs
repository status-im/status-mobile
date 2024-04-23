(ns quo.components.wallet.confirmation-progress.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:total-box {:optional true} [:maybe :int]]
      [:counter {:optional true} [:maybe :int]]
      [:progress-value {:optional true} [:maybe :int]]
      [:network {:optional true} [:enum :mainnet :optimism :arbitrum]]
      [:state {:optional true} [:enum :pending :sending :confirmed :finalising :finalized :error]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]]
   :any])
