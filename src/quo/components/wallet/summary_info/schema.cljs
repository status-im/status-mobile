(ns quo.components.wallet.summary-info.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:type [:enum :status-account :saved-account :account :user :token :network]]
      [:account-props {:optional true} [:maybe :map]]
      [:network-props {:optional true} [:maybe :map]]
      [:token-props {:optional true} [:maybe :map]]
      [:networks-to-show {:optional true} [:maybe :map]]]]]
   :any])
