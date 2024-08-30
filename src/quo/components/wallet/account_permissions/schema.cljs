(ns quo.components.wallet.account-permissions.schema
  (:require [quo.components.wallet.required-tokens.schema :as required-tokens-schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:account
       [:map
        [:name [:maybe :string]]
        [:address [:maybe :string]]
        [:emoji [:maybe :string]]
        [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]
      [:token-details {:optional true} [:maybe [:sequential required-tokens-schema/?schema]]]
      [:keycard? {:optional true} [:maybe :boolean]]
      [:checked? {:optional true} [:maybe :boolean]]
      [:disabled? {:optional true} [:maybe :boolean]]
      [:on-change {:optional true} [:maybe fn?]]
      [:container-style {:optional true} [:maybe :map]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]]
   :any])
