(ns quo.components.wallet.address-text.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:address {:optional true} [:maybe :string]]
      [:blur? {:optional true} [:maybe :boolean]]
      [:format {:optional true} [:enum :short :long]]
      [:networks {:optional true}
       [:maybe [:sequential [:map [:network-name :keyword] [:short-name :string]]]]]
      [:full-address? {:optional true} [:maybe :boolean]]
      ;; TODO: size and weight are text schemas and should be imported here
      ;; https://github.com/status-im/status-mobile/issues/19443
      [:size {:optional true} [:maybe :keyword]]
      [:weight {:optional true} [:maybe :keyword]]]]]
   :any])
