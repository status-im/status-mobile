(ns quo.components.wallet.address-text.schema)

(def ?schema
  [:=>
   [:catn
    [:props
     [:schema.common/map {:optional true :maybe true}
      [:address :string]
      [:blur? :boolean]
      [:format {:no-maybe true} [:enum :short :long]]
      [:networks [:sequential [:map [:network-name :keyword] [:short-name :string]]]]
      [:full-address? :boolean]
      ;; TODO: size and weight are text schemas and should be imported here
      ;; https://github.com/status-im/status-mobile/issues/19443
      [:size :keyword]
      [:weight :keyword]]]]
   :any])
