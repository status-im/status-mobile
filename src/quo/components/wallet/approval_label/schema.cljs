(ns quo.components.wallet.approval-label.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:status [:enum :approve :approving :approved]]
      [:token-value :string]
      [:token-symbol :string]
      [:container-style {:optional true} [:maybe :map]]
      [:show-view-button? {:optional true} [:maybe :boolean]]
      [:button-props {:optional true} [:maybe :map]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]]
   :any])
