(ns quo.components.wallet.account-permissions.schema
  (:require [quo.components.wallet.required-tokens.view :as required-tokens]))

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
        [:customization-color [:maybe [:or :string :keyword]]]]]
      [:token-details {:optional true} [:maybe [:vector required-tokens/?schema]]]
      [:keycard? {:optional true} [:maybe :boolean]]
      [:checked? {:optional true} [:maybe :boolean]]
      [:disabled? {:optional true} [:maybe :boolean]]
      [:on-change {:optional true} [:maybe fn?]]
      [:container-style {:optional true} [:maybe :map]]
      [:theme :schema.common/theme]]]]
   :any])
