(ns quo.components.wallet.network-bridge.schema)

(def ^:private ?network-bridge-status
  [:enum :add :loading :locked :disabled :default])

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:theme :schema.common/theme]
      [:network {:optional true} [:maybe :keyword]]
      [:status {:optional true} [:maybe ?network-bridge-status]]
      [:amount {:optional true} [:maybe :string]]
      [:container-style {:optional true} [:maybe :map]]
      [:on-press {:optional true} [:maybe fn?]]]]]
   :any])
