(ns quo.components.wallet.summary-info.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:type [:enum :status-account :saved-account :account :user :token]]
      [:account-props {:optional true} [:maybe :map]]
      [:token-props {:optional true} [:maybe :map]]
      [:networks? {:optional true} [:maybe :boolean]]
      [:values {:optional true} [:maybe :map]]]]]
   :any])
