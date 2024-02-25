(ns quo.components.wallet.summary-info.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:theme :schema.common/theme]
      [:type [:enum :status-account :saved-account :account :user]]
      [:account-props {:optional true} [:maybe :map]]
      [:networks? {:optional true} [:maybe :boolean]]
      [:values {:optional true} [:maybe :map]]]]]
   :any])
